
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SolutionTest {

	private final File file;
	private Solution sol;

	public SolutionTest(File file) {
		this.file = file;
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { new File("input00.txt") } };
		return Arrays.asList(data);
	}

	@Test
	public void testGetLines() throws FileNotFoundException, IOException {
		sol = new Solution(new FileInputStream(file), System.out);
		sol.readInput();
		sol.printInput();
		sol.calcLines();
		sol.printOutput();
		double md = sol.getMaxDistance();
		System.out.println(md);
	}

}
