package AOS_HW1;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import AOS_HW1.RandomReferenceString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Algorithm {
	private int frameSize;
	private static int pageFaults;
	private static int diskWrites;
	private static int interrupts;
	private int count;

	public Algorithm(int frameSize) {
		this.frameSize = frameSize;
		this.pageFaults = 0;
		this.diskWrites = 0;
		this.interrupts = 0;
		this.count = 0;
	}

	public void Fifo_Simulate(ArrayList<Integer> referenceString, int[] frames, int[] dirty_bit_string) {
		// Map<page,location>
		Map<Integer, Integer> frameMap = new HashMap<>();	//frameMap:用於追蹤目前駐留在frames的pages
		int[] dirty_bit = new int[frameSize];	//dirty_bit的大小根據逐輪frameSize之大小而變動

		for (int i = 0; i < referenceString.size(); i++) {	//逐一比較referenceString中之reference是否hit
			int page = referenceString.get(i);

			if (frameMap.containsKey(page)) { // Page hit
				int frameIndex = frameMap.get(page);
				if (dirty_bit_string[i] == 1 && dirty_bit[frameIndex] == 0) {
					dirty_bit[frameIndex] = 1;
				}

			} else { // Page fault

				pageFaults++;

				if (frameMap.size() < frameSize) { // Frames are not full
					int nextFrameIndex = frameMap.size(); // frameMap.size最一開始等於0 //frameMap.size等於1
					frameMap.put(page, nextFrameIndex);   // frameMap.put(page, 0); //frameMap.put(page, 1);
					frames[nextFrameIndex] = page;        // frames[0] = page; //frames[1] = page;
					dirty_bit[nextFrameIndex] = dirty_bit_string[i];
					
				} else { // Frames are full 
					int victimPage = frames[count % frameSize];		 // Assume frame size=20, 在第 21 round ----> frames[20 % 20]
																	 // victimPage = frames[count % frameSize] = frames[0]
					int victimIndex = frameMap.get(victimPage);		 // victimIndex = frameMap.get(0);
					if (dirty_bit[victimIndex] == 1) { 				 // 在第 22 round ----> int victimPage = frames[21 % 20] = frames[1]
						diskWrites++;							     // victimIndex = frameMap.get(1);
					}
					frameMap.remove(victimPage);
					frameMap.put(page, victimIndex);
					frames[victimIndex] = page;
					dirty_bit[victimIndex] = dirty_bit_string[i];
				}
			}

			count++;
		}
	}

	public void ARB_Simulate(ArrayList<Integer> referenceString, int[] frames, int[] dirty_bit_string) {

		int[] dirty_bit = new int[frameSize];
		int[] reference_bit = new int[frameSize];
		int[] ARB_bit = new int[frameSize];
		int ARB_Size = 8; // ARB(8-bits)
		int clock = 10; // 每過一個 clock就更新ARB

		// 逐一比對referenceString和frame裡面有沒有資料相符
		for (int i = 0; i < referenceString.size(); i++) {
			for (int j = 0; j < frameSize; j++) {
				if (referenceString.get(i) == frames[j]) { // hit
					// frame裡面的dirty_bit是0,但讀進來的dirty_bit是1
					if (dirty_bit_string[i] == 1 && dirty_bit[j] == 0) {
						dirty_bit[j] = 1; // 把frame裡面的dirty_bit改為1
					}
					reference_bit[j] = 1;				
					break;

				} else if ((referenceString.get(i) != frames[j]) && (j == (frames.length - 1))) { // 單一reference測到frame最尾端依舊沒有hit---->page fault
																									
					pageFaults++;

					if (frames[0] == 0) { // frames is not full

						for (int k = 0; k < frames.length - 1; k++) {
							frames[k] = frames[k + 1];
							dirty_bit[k] = dirty_bit[k + 1];
							reference_bit[k] = reference_bit[k + 1];
						}
						frames[frames.length - 1] = referenceString.get(i);
						dirty_bit[dirty_bit.length - 1] = dirty_bit_string[i];
						reference_bit[reference_bit.length - 1] = 1;
					
					} else { // frames is full

						int victim_page = 0;

						for (int x = 0; x < frames.length; x++) {
							if (ARB_bit[x] < ARB_bit[victim_page])
								victim_page = x;
						}

						if (dirty_bit[victim_page] == 1)
							diskWrites++;

						for (int k = victim_page; k < frames.length - 1; k++) {
							frames[k] = frames[k + 1];
							dirty_bit[k] = dirty_bit[k + 1];
							reference_bit[k] = reference_bit[k + 1];
						}
						
						frames[frames.length - 1] = referenceString.get(i);
						dirty_bit[dirty_bit.length - 1] = dirty_bit_string[i];
						reference_bit[reference_bit.length - 1] = 1;					
					}
				}
			}

			if (i % 10 == 0) { //Assume clock = 10, 每過10輪更新
				for (int j = 0; j < frames.length; j++) {
					ARB_bit[j] >>= 1; // 右移空出最高位元
					ARB_bit[j] |= (reference_bit[j] << (ARB_Size - 1)); // Reference bit 移到最高位元
					reference_bit[j] = 0; // Reference bit reset 為 0
				}
				interrupts++;
			}
		}
	}

	public void ESC_Simulate(ArrayList<Integer> referenceString, int[] frames, int[] dirty_bit_string) {

		int[] dirty_bit = new int[frameSize];
		int[] reference_bit = new int[frameSize];

		// 逐一比對referenceString和frame裡面有沒有資料相符
		for (int i = 0; i < referenceString.size(); i++) {
			for (int j = 0; j < frameSize; j++) {
				if (referenceString.get(i) == frames[j]) { // hit
					// frame裡面的dirty_bit是0,但讀進來的dirty_bit是1
					if (dirty_bit_string[i] == 1 && dirty_bit[j] == 0) {
						dirty_bit[j] = 1; // 把frame裡面的dirty_bit改為1
					}
					reference_bit[j] = 1; //// 被參考過了----> reference_bit改成1
					break;
				} else if ((referenceString.get(i) != frames[j]) && (j == (frames.length - 1))) { // 單一reference測到frame最尾端依舊沒有hit---->page fault
																									

					pageFaults++;

					if (frames[0] == 0) { // frames is not full
						// 把全部frame, dirty_bit, reference_bit往前挪一格
						for (int k = 0; k < frames.length - 1; k++) {
							frames[k] = frames[k + 1];
							dirty_bit[k] = dirty_bit[k + 1];
							reference_bit[k] = reference_bit[k + 1];
						}
						// 把新的放到frame, dirty_bit, reference_bit放到陣列最尾端
						frames[frames.length - 1] = referenceString.get(i);
						dirty_bit[dirty_bit.length - 1] = dirty_bit_string[i];
						reference_bit[reference_bit.length - 1] = 1;

					} else { // frames is full

						int replaceFrameIndex = -1; // reference
						int priority = 0; // 00---->0分, 01---->1分, 10---->2分, 11---->3分
						int x = 4; // 紀錄當前分數最小
						int oneZeroCount = 0; // 10 出現次數
						int oneOneCount = 0; // 11 出現次數

						for (int k = 0; k < frames.length; k++) {
							if (reference_bit[k] == 0 && dirty_bit[k] == 0) {
								priority = 0;
								if (priority < x) {
									replaceFrameIndex = k;
									x = priority;
								}
							} else if (reference_bit[k] == 0 && dirty_bit[k] == 1) {
								priority = 1;
								if (priority < x) {
									replaceFrameIndex = k;
									x = priority;
								}
							} else if (reference_bit[k] == 1 && dirty_bit[k] == 0) {
								priority = 2;
								if (priority < x) {
									replaceFrameIndex = k;
									x = priority;
								}
								oneZeroCount++;
							} else {
								priority = 3;
								if (priority < x) {
									replaceFrameIndex = k;
									x = priority;
								}
								oneOneCount++;
							}
						}

						if (oneZeroCount == frames.length || oneOneCount == frames.length) { // 大家都是10或11
							for (int frameIndex = 0; frameIndex < reference_bit.length; frameIndex++)
								reference_bit[frameIndex] = 0; // 把reference_bit都清空
							interrupts++;
						}

						if (dirty_bit[replaceFrameIndex] == 1)
							diskWrites++;

						for (int k = replaceFrameIndex; k < frames.length - 1; k++) {
							frames[k] = frames[k + 1];
							dirty_bit[k] = dirty_bit[k + 1];
							reference_bit[k] = reference_bit[k + 1];
						}

						frames[frames.length - 1] = referenceString.get(i);
						dirty_bit[dirty_bit.length - 1] = dirty_bit_string[i];
						reference_bit[reference_bit.length - 1] = 1;

					}
				}
			}
		}
	}

	public void My_Algo_Simulate(ArrayList<Integer> referenceString, int[] frames, int[] dirty_bit_string) {

		int[] dirty_bit = new int[frameSize];
		int[] counter = new int[frameSize];
		// 逐一比對referenceString和frame裡面有沒有資料相符
		for (int i = 0; i < referenceString.size(); i++) {
			for (int j = 0; j < frameSize; j++) {
				if (referenceString.get(i) == frames[j]) { // hit
					// frame裡面的dirty_bit是0,但讀進來的dirty_bit是1
					if (dirty_bit_string[i] == 1 && dirty_bit[j] == 0) {
						dirty_bit[j] = 1; // 把frame裡面的dirty_bit改為1
					}
					counter[j]++;
					
					int x = 100000;	 	//x為當前最小的counter值，用來比較誰應該被swap
					int swap = 0;		//應該被swap的存放在swap變數
					for(int k = counter.length - 1 ; k > j; k--) {	//從frame陣列中最後一格往前比較到frames[j]的前一格				
						if (counter[k] < x) {	//若counter[k] < 當前最小的counter值
							swap = k;			//則將k放到預計要被swap的swap變數
							x = counter[k];		//並將x的值改設為k的counter
						}																
					}
					//在確定了要被swap的欄位後
					//分別把當下frames[j]在frames, dirty_bit, counter陣列的資料和選定的frames[swap]做交換
					int tmp = frames[j];
					frames[j] = frames[swap];
					frames[swap] = tmp;

					int t = dirty_bit[j];
					dirty_bit[j] = dirty_bit[swap];
					dirty_bit[swap] = t;

					int c = counter[j];
					counter[j] = counter[swap];
					counter[swap] = c;						
						
				} else if ((referenceString.get(i) != frames[j]) && (j == (frames.length - 1))) { // 單一reference測到frame最尾端依舊沒有hit---->page fault
																									
					pageFaults++;
					// 要被replace的page之dirty_bit=1---->diskWrites++
					if (dirty_bit[0] == 1)
						diskWrites++;
					// 把全部frame, dirty_bit往前挪一格
					for (int k = 0; k < frames.length - 1; k++) {
						frames[k] = frames[k + 1];
						dirty_bit[k] = dirty_bit[k + 1];
						counter[k] = counter[k + 1];
					}
					// 把新的放到frame, dirty_bit放到陣列最尾端
					frames[frames.length - 1] = referenceString.get(i);
					dirty_bit[dirty_bit.length - 1] = dirty_bit_string[i];
					counter[counter.length - 1] = 1; 	//新進的page將其counter設定為1
				}
			}
		}
	}

	public int getPageFaults() {
		return pageFaults;
	}

	public int getDiskWrites() {
		return diskWrites;
	}

	public int getInterrupt() {
		return interrupts + pageFaults + diskWrites;
	}

	public static void main(String[] args) {

		// 生成200000筆referenceString
		ArrayList<Integer> referenceString = RandomReferenceString.ReferenceString();
		System.out.println(referenceString);

		int[] frameSizes = { 20, 40, 60, 80, 100 };

		for (int frameSize : frameSizes) {
			// frames陣列:進入simulate之長度為20, 40, 60, 80, 100之frames
			int[] frames = new int[frameSize];
			int[] dirty_bit_string = RandomReferenceString.dirty_bit_string();

			Algorithm algo = new Algorithm(frameSize);
			algo.Fifo_Simulate(referenceString, frames, dirty_bit_string);
			System.out.println("Fifo : ");
			System.out.println("Frame Size : " + frameSize);
			System.out.println("Page Faults : " +algo.getPageFaults());
			System.out.println("Disk Writes : " +algo.getDiskWrites());
			System.out.println("Interrupts : "+ algo.getInterrupt());
			System.out.println("============================");
			pageFaults = 0;
			diskWrites = 0;
			interrupts = 0;

			algo.ARB_Simulate(referenceString, frames, dirty_bit_string);
			System.out.println("ARB : ");
			System.out.println("Frame Size : " + frameSize);
			System.out.println("Page Faults : " +algo.getPageFaults());
			System.out.println("Disk Writes : " +algo.getDiskWrites());
			System.out.println("Interrupts : "+ algo.getInterrupt());
			System.out.println("============================");
			pageFaults = 0;
			diskWrites = 0;
			interrupts = 0;

			algo.ESC_Simulate(referenceString, frames, dirty_bit_string);
			System.out.println("ESC : ");
			System.out.println("Frame Size : " + frameSize);
			System.out.println("Page Faults : " +algo.getPageFaults());
			System.out.println("Disk Writes : " +algo.getDiskWrites());
			System.out.println("Interrupts : "+ algo.getInterrupt());
			System.out.println("============================");
			pageFaults = 0;
			diskWrites = 0;
			interrupts = 0;

			algo.My_Algo_Simulate(referenceString, frames, dirty_bit_string);
			System.out.println("My Algo : ");
			System.out.println("Frame Size : " + frameSize);
			System.out.println("Page Faults : " +algo.getPageFaults());
			System.out.println("Disk Writes : " +algo.getDiskWrites());
			System.out.println("Interrupts : "+ algo.getInterrupt());
			System.out.println("============================");
			pageFaults = 0;
			diskWrites = 0;
			interrupts = 0;
		}
	}
}










