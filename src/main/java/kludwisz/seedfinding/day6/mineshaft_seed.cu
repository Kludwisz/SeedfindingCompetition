/*
The following file is a CUDA kernel that we used to find mineshaft carver seeds that generate
a long corridor with a bunch of spider spawners. It exploits a weakness in Java Random that
allows iteration over states satisfying certain PRNG outpu conditions. In this case, the
condition is that a mineshaft generates, which is equivalent to
    rand.nextDouble() < 0.004
After some transformations, we obtain a direct way to iterate PRNG states such that the above
condition always evaluates to true.
With this optimization, the entire kernel takes around half a minute to run.
*/


#include "jrand/jrand.cuh"
#include "cudaSetup.cuh"
#include <cmath>

constexpr double MAX_VALUE = 0.004 * (double)(1ULL << 53u);
constexpr uint64_t MAX_UPPER_26 = 268435;
constexpr uint64_t MAX_TID = MAX_UPPER_26 * (1ULL << 22u);
constexpr uint64_t RUN_SIZE = 1ULL << 31;
constexpr int RUNS = (MAX_TID + RUN_SIZE - 1) / RUN_SIZE;


__global__ void getMineshaftCarvers(uint64_t offset) {
	const uint64_t tid = offset + (uint64_t)blockIdx.x * blockDim.x + threadIdx.x;
	if (tid >= MAX_TID) return;

	const uint64_t lower22 = tid & 0x3FFFFFULL;
	const uint64_t upper26 = tid >> 22;
	const uint64_t initialSeed = (upper26 << 22) | lower22;

	uint64_t state = initialSeed;
	advance(&state); // skip call #2 in nextDouble

	advance2(&state);
	advance3(&state);

	int long_corrs = 0;
	for (int i = 0; i < 4; i++)
	{
		if (nextInt(&state, 100) >= 70) return; // only corridors
		int l = nextInt(&state, 3);
		if (l != 0) return; // short corridors only
		//if (l == 2) return; // dont want long ones
		//if (l == 1 && (++long_corrs >= 2)) return; // dont want long ones
		if (nextInt(&state, 3) == 0) return; // no rails
		if (nextInt(&state, 23) != 0) return; // cobwebs
		if (i != 3 && nextInt(&state, 4) > 1) return; // next piece same direction (north)
		advance(&state); // height diff unimportant
	}

	state = initialSeed;
	goBack(&state);
	state ^= JRAND_MULTIPLIER;
	const uint64_t carverSeed = (state & MASK48);

	// check trial chamber params (it will be in the same carver chunk!)
	state = carverSeed ^ JRAND_MULTIPLIER;
	int y = nextInt(&state, 21) - 41;
	//if (y < -37 || y > -35) return; // y coord bad
	int rot = nextInt(&state, 4);
	if (rot != 0) return; // rotation is not north

	printf("%llu  y = %d\n", carverSeed, y);
}

// ----------------------------------------------------------------

int findSpiderCorridors() {
	//printf("upper 26 bits at most: %llu\n", ((uint64_t)round(MAX_VALUE)) >> 27);
	//return 0;

	CHECK_ERR(cudaSetDevice(0));

	for (int run = 0; run < RUNS; run++)
	{
		const int THREADS_PER_BLOCK = 512;
		const int N_BLOCKS = (int)((RUN_SIZE + THREADS_PER_BLOCK - 1) / THREADS_PER_BLOCK);
		getMineshaftCarvers <<< N_BLOCKS, THREADS_PER_BLOCK >>> (run * RUN_SIZE);
		CHECK_ERR(cudaGetLastError());
		CHECK_ERR(cudaDeviceSynchronize());

		//fprintf(stdout, "----- (stdout) Run %d/%d\n", run + 1, RUNS);
		fprintf(stderr, "----- (stderr) Run %d/%d\n", run + 1, RUNS);
	}

	return 0;
}

// ----------------------------------------------------------------

int main() {
	return findSpiderCorridors();
}