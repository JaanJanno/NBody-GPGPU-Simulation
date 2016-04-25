package ee.ut.jjanno.perftests;

import java.util.ArrayList;
import java.util.List;

import ee.ut.jjanno.gpusimulation.GravitySystemGPUExecutor;
import ee.ut.jjanno.simulation.Body;
import ee.ut.jjanno.simulation.GravitySystem;

public class SimTest {

	public static void main(String[] args) {

		int start = 1000;
		int end = 40000;
		int step = 1000;
		int runs = 10;

		// QTree GPU
		for (int n = start; n < end; n += step) {
			List<Body> bodies = genOrbitals(n);
			GravitySystemGPUExecutor.executeAdvancedPlus(bodies);
			long startT = System.nanoTime();
			for (int i = 0; i < runs; i++)
				GravitySystemGPUExecutor.executeAdvancedPlus(bodies);
			System.out.print(n);
			System.out.print(';');
			System.out.println((System.nanoTime() - startT) / 1000000 / runs);
		}

		// N^2 GPU
		for (int n = start; n < end; n += step) {
			List<Body> bodies = genOrbitals(n);
			GravitySystemGPUExecutor.execute(bodies);
			long startT = System.nanoTime();
			for (int i = 0; i < runs; i++)
				GravitySystemGPUExecutor.execute(bodies);
			System.out.print(n);
			System.out.print(';');
			System.out.println((System.nanoTime() - startT) / 1000000 / runs);
		}

		// N^2 CPU
		for (int n = start; n < end; n += step) {
			List<Body> bodies = genOrbitals(n);
			long startT = System.nanoTime();
			for (int i = 0; i < runs; i++)
				GravitySystem.simulate(bodies);
			System.out.print(n);
			System.out.print(';');
			System.out.println((System.nanoTime() - startT) / 1000000 / runs);
		}

	}

	private static List<Body> genOrbitals(int n) {
		List<Body> testBodies = new ArrayList<>();
		GravitySystem.generateOrbitals(testBodies, n, 0, 0, 1000, 10000);
		return testBodies;
	}

}
