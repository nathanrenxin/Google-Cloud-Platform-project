package project.id2210.project_id2210;

import java.util.ArrayList;

public class Runner {
	
	public static void main(final String[] args) {
		
		String[] argsC = {"0","createBucket","my-bucket-id2210"};
		try {
			MyStorage.main(argsC);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    int numberOfSimultaneousExecutions = args.length==0?1:Integer.valueOf(args[0]);
	    int fiveSecondsGap = args.length==0?0:Integer.valueOf(args[1]);
	    java.util.concurrent.Executor executor = java.util.concurrent.Executors.newFixedThreadPool(numberOfSimultaneousExecutions*3);
	    for (int i = 1; i < numberOfSimultaneousExecutions+1; i++) {
	    	final int j=i;
	    	if(fiveSecondsGap==1) {
	    		try {
	    			Thread.sleep(5000);
	    		} catch (InterruptedException e2) {
	    			// TODO Auto-generated catch block
	    			e2.printStackTrace();
	    		}
	    	}
	        executor.execute(new Runnable() {
	            @Override
	            public void run() {
	                try {
	                	//System.out.printf("current j: %s %n", j);
	                	String[] argsU = {Integer.toString(j),"upload",".\\upload\\"+Integer.toString(j),"my-bucket-id2210"};
						MyStorage.main(argsU);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
	        });
	    }
	    try {
			Thread.sleep(100000*numberOfSimultaneousExecutions);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	    for (int i = 1; i < numberOfSimultaneousExecutions+1; i++) {
	    	final int j=i;
	    	if(fiveSecondsGap==1) {
	    		try {
	    			Thread.sleep(5000);
	    		} catch (InterruptedException e2) {
	    			// TODO Auto-generated catch block
	    			e2.printStackTrace();
	    		}
	    	}
	        executor.execute(new Runnable() {
	            @Override
	            public void run() {
	                try {
	                	String[] argsDo = {Integer.toString(j),"download","my-bucket-id2210",Integer.toString(j),".\\download"};
	                	MyStorage.main(argsDo);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
	        });
	    }
	    try {
	    	Thread.sleep(70000*numberOfSimultaneousExecutions);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	    for (int i = 1; i < numberOfSimultaneousExecutions+1; i++) {
	    	final int j=i;
	        executor.execute(new Runnable() {
	            @Override
	            public void run() {
	                try {
	                	String[] argsDe = {Integer.toString(j),"delete","my-bucket-id2210",Integer.toString(j)};
						MyStorage.main(argsDe);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
	        });
	    }
	    try {
			Thread.sleep(10000);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		String[] argsD = {"0","deleteBucket","my-bucket-id2210"};
		try {
			MyStorage.main(argsD);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}

