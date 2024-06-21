package AOS_HW1;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class RandomReferenceString {
	
	public static int[] dirty_bit_string() {
		
		Random random = new Random();
		int[] dirty_bit_string = new int[200000];
		// Random dirty bit
		for (int i = 0; i < dirty_bit_string.length; i++) {
			dirty_bit_string[i] = random.nextInt(2);
		}
		return dirty_bit_string;
		
	}

	public static ArrayList<Integer> ReferenceString() {

		System.out.println("選擇Reference String(0:Random, 1:Locality, 2:My own reference string 3:Finish)");
		Scanner scanner = new Scanner(System.in);
		int s = scanner.nextInt();

		Random random = new Random();

		ArrayList<Integer> a = new ArrayList<Integer>();
		ArrayList<Integer> finalReferenceString = new ArrayList<Integer>();
		int[] Locality = new int[200000];
		
	
		switch(s) {		
		
			case 0:	//Random	
				int count = 0;
				while(count<199000) {
					int referenceLength = random.nextInt(20);		//介於0~19的referenceLength
			    	int referenceString = random.nextInt(1000-referenceLength+1);	//生成介於1~(1000-referenceLength+1)之間的隨機起始點的referenceString
					for(int i = referenceString; i < referenceString+referenceLength; i++) {
						finalReferenceString.add(i);
			    		count++;
			    		if(count==200000) break;
			    	}
				}
				return finalReferenceString;
				
		    	
			case 1:	//Locality
				for(int j = 0; j < 200; j++) {	//200000筆資料切成200份，一份1000
					int referenceX = random.nextInt(18)+33;		//介於(0-17)+33---->[33-50]的referenceX
					int referenceNum = random.nextInt(1000-referenceX+1);	//介於1~(1000-X+1)的referenceNum
					for(int i = 0; i < 1000; i++) {
						//在referenceNum~referenceNum+referenceX之間取一個數
						int referenceString = random.nextInt(referenceX+1)+referenceNum;
						finalReferenceString.add(referenceString);			    		
			    	}			
				}					
				return finalReferenceString;
	        
			case 2:	//My reference string	
				count = 0;
				while(count<199000) {
					int referenceLength = random.nextInt(100);		//介於0~100的referenceLength
			    	int referenceString = random.nextInt(1000-referenceLength+1);	//生成介於1~(1000-referenceLength+1)之間的隨機起始點的referenceString
					//從即將開始跑的ReferenceString中隨機選擇一個幸運號碼，並放入finalReferenceString中
			    	int luckyNum = random.nextInt(1000-referenceLength+1)+referenceLength;					
					finalReferenceString.add(luckyNum);
					count++;
					finalReferenceString.add(luckyNum);
					count++;
					
			    	for(int i = referenceString; i < referenceString+referenceLength; i++) {
						finalReferenceString.add(i);
			    		count++;
			    		if(count==200000) break;
			    	}
				}
				return finalReferenceString;
			    
			case 3:
				System.out.println("Finish!!!");
				break;

			default:
				System.out.println("輸入錯誤!!!");
				break;
				
		}
		return ReferenceString();	    
	}
}


//純Random
//for(int i = 0; i < Locality.length; i++) {
//	Locality[i] = random.nextInt(1000);	
//	finalReferenceString.add(Locality[i]);
//}
//return finalReferenceString;

//Locality
//int x = 0, y = 0;
//for (int i = 0; i < 5; i++) {	// 200000切成5等分
//    x = random.nextInt(980);	//0~979
//    for (int j = 0; j < 40000; j++) {
//        y = (random.nextInt(20)) + 1;   //1~20
//        Locality[(i * 40000) + j] = x + y;
//    }
//}
//for(int i=0;i<Locality.length;i++) {
//	finalReferenceString.add(Locality[i]);
//}
//return finalReferenceString;


























