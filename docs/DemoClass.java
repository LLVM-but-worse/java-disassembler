class DemoClass {
	public static void main(String[] args) {
		System.out.println("This is a pretty nasty control and data flow!");
		int x = 1;
		int y = 2;
		int z = 3;
		label:
		try {
			x = 1;
			label2: while (true) {
				try {
					y = x;
					System.out.print("This will always be 1: ");
					System.out.println(y);
					break label2;
				} catch(Exception e2) {
					x = y;
					System.out.print("This will always be 1: ");
					System.out.println(x);
					break label;
				} finally {
					System.out.print("This will always be 2: ");
					z = 2;
				}
			}
			System.out.print("This will always be 2: ");
			System.out.println(z);
		} catch(Exception e) {
			z = x;
		} finally {
			System.out.println(z);
		}
		System.out.println(x);
		System.out.println(y);
		System.out.println(z);
	}
}