/*
Gameboy Audio Encoder 2.0, Java encoder build 2.6, 2/9/2021
Made by Jackson Shelton
See README for OS-dependent(?) changes for command line and
how to compile for your system.

Quantization for legacy lo-fi mode has been fixed, and support
for both legacy and HQ now spands across both original GB
and GBC
*/

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class encoder
{
	public static void createSource(int[] arguments)
	{
		int thissys = arguments[0];
		int mode = arguments[1];
		int numchans = arguments[2];
		int div = arguments[3];
		String mainsource = "";
		if(thissys==0)
		{
			if(numchans==1)
			{
				if(mode==0)
				{	
					mainsource = "monogb.asm";
				}
				else
				{
					mainsource = "monogbhq.asm";
				}
			}
			else
			{
				if(mode==0)
				{
					mainsource = "stereogb.asm";
				}
				else
				{
					mainsource = "stereogbhq.asm";
				}
			}
		}
		else
		{
			if(numchans==1)
			{
				if(mode==0)
				{
					mainsource = "monolegacy.asm";
				}
				else
				{
					mainsource = "monohq.asm";
				}
			}
			else
			{
				if(mode==0)
				{
					mainsource = "stereolegacy.asm";
				}
				else
				{
					mainsource = "stereohq.asm";
				}
			}
		}
		try
		{
			BufferedReader srcreader = new BufferedReader(new FileReader(mainsource));
			File sourcecopy = new File("audio.asm");
			BufferedWriter srcwriter = new BufferedWriter(new FileWriter(sourcecopy));
			String line = "";
			String dividerLine = ("\tld a, " + div);
			String outline = "";
			while((line=srcreader.readLine())!=null)
			{
				outline = line;
				if(line.indexOf("timer divider")!=-1)
				{
					outline = dividerLine;
				}
				srcwriter.write(outline);
				srcwriter.newLine();
			}
			srcwriter.flush();
			srcreader.close();
			srcwriter.close();
		}catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	//system ID, if applicable, is the -C that makes the ROM GBC-specific in the header
	public static void createHeader(int system)
	{
		String systemID;
		String romsuffix;
		if(system==0)
		{
			romsuffix = ".gb";
			systemID = "";
		}
		else
		{
			romsuffix = ".gbc";
			systemID = "-C";
		}
		try{	
			String romstring = ("audio" + romsuffix);
			String filepath = System.getProperty("user.dir");
			try{	
				Process s = new ProcessBuilder().command("bash", "-c", "rgbasm -o audio.o audio.asm").inheritIO().start();
				s.waitFor();
				Process q = new ProcessBuilder().command("bash", "-c", "rgblink -o " + romstring + " audio.o").inheritIO().start();
				q.waitFor();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	//since the pulse value changes first, I reference the previous master volume value
	public static int[] getBestValue(int prevmv, int prevsamp, int currsamp, ArrayList<Integer>[] lut)
	{
		int[] possibleconfigs = new int[(lut[currsamp].size())/2];
		int[] differences = new int[(lut[currsamp].size())/2];
		int[] output = new int[2];
		int tempsamp = 0;
		int temppulse = 7;
		
		for(int i=0; i<(lut[currsamp].size()/2); i++)
		{
			possibleconfigs[i]=lut[currsamp].get(i*2);
		}
		for(int i=0; i<possibleconfigs.length; i++)
		{ 
			temppulse = possibleconfigs[i];
			if(temppulse>7)
			{
				tempsamp = (int)(128.0*((((double)temppulse-7.0)/7.0)*(((double)(prevmv+1))/8.0)));
				tempsamp = tempsamp + 127;	
			}
			if(temppulse<8)
			{
				tempsamp = (int)(128.0*(((8.0-(double)temppulse)/7.0)*(((double)(prevmv+1))/8.0)));
				tempsamp = tempsamp + 128;
			}
			if(currsamp>prevsamp)
			{
				if(tempsamp>=prevsamp && tempsamp<=currsamp)
				{
					output[0] = lut[currsamp].get(i*2);
					output[1] = lut[currsamp].get((i*2)+1);
					return output;
				}
				if(tempsamp<prevsamp)
				{
					differences[i] = prevsamp-tempsamp;
				}
				if(tempsamp>currsamp)
				{
					differences[i] = tempsamp-currsamp;
				}
			}
			else
			{
				if(tempsamp<=prevsamp && tempsamp>=currsamp)
				{
					output[0] = lut[currsamp].get(i*2);
					output[1] = lut[currsamp].get((i*2)+1);
					return output;
				}
				if(tempsamp>prevsamp)
				{
					differences[i] = tempsamp-prevsamp;
				}
				if(tempsamp<currsamp)
				{
					differences[i] = currsamp-tempsamp;
				}
			}
		}
		int bestdiff = 256;
		int bestindex = 0;
		for(int i=0; i<differences.length; i++)
		{
			if(differences[i]<bestdiff)
			{
				bestdiff = differences[i];
				bestindex = i;
			}
		}
		/*
		2/7/2021- added this code to reduce the amount of noise in the output audio
		due to the pulse-triggered amplitude being higher than intended when increasing
		and lower than intended when decreasing
		2/8/2021- this code is now pretty much obsolete due to fixed LUT having much better
		output quality
		*/
		int idealdiff = 6;
		int calcsamp1 = 0;
		int calcsamp2 = 0;
		int tempadd = 0;
		boolean firstcheck = false;
		boolean secondcheck = false;
		if(bestdiff>0)
		{
			if(currsamp>prevsamp)
			{
				if(bestdiff>(currsamp-prevsamp))
				{
					idealdiff = (currsamp-prevsamp);
				}
				for(int i=currsamp; i>(currsamp-idealdiff); i--)
				{
					for(int k=0; k<lut[i].size(); k+=2)
					{
						for(int h=0; h<lut[prevsamp].size(); h+=2)
						{
							if(lut[i].get(k)==lut[prevsamp].get(h))
							{
								calcsamp1 = lut[prevsamp].get(h);
								if(calcsamp1<8)
								{
									calcsamp1 = -1*(8-calcsamp1);
									tempadd = 128;
								}
								else
								{
									calcsamp1 = calcsamp1-7;
									tempadd = 127;
								}
								calcsamp1 = (int)(((double)tempadd)+(128.0*((((double)calcsamp1)/7.0)*(((double)(lut[prevsamp].get(h+1)+1))/8.0))));
								firstcheck = true;
							}
							if(lut[i].get(k+1)==lut[prevsamp].get(h+1))
							{
								calcsamp2 = lut[prevsamp].get(h);
								if(calcsamp2<8)
								{
									calcsamp2 = -1*(8-calcsamp2);
									tempadd = 128;
								}
								else
								{
									calcsamp2 = calcsamp2-7;
									tempadd = 127;
								}
								calcsamp2 = (int)(((double)tempadd)+(128.0*((((double)calcsamp2)/7.0)*(((double)(lut[i].get(k+1)))/8.0))));
								secondcheck = true;
							}
							if(firstcheck==true && secondcheck==true)
							{
								if(calcsamp1>=calcsamp2 && currsamp>=calcsamp1 && calcsamp1>=prevsamp)//because the current amplitude is greater than the previous one
								{
									output[0] = lut[i].get(k);
									output[1] = lut[prevsamp].get(h+1);
									return output;
								}
								if(calcsamp1<calcsamp2 && currsamp>=calcsamp2 && calcsamp2>=prevsamp)
								{
									output[0] = lut[prevsamp].get(h);
									output[1] = lut[i].get(k+1);
									return output;
								}
							}
						}
					}
				}
			}
			else
			{
				if(bestdiff>(prevsamp-currsamp))
				{
					idealdiff = (prevsamp-currsamp);
				}
				for(int i=currsamp; i<(currsamp+idealdiff); i++)
				{
					for(int k=0; k<lut[i].size(); k+=2)
					{
						for(int h=0; h<lut[prevsamp].size(); h+=2)
						{
							if(lut[i].get(k)==lut[prevsamp].get(h))
							{
								calcsamp1 = lut[prevsamp].get(h);
								if(calcsamp1<8)
								{
									calcsamp1 = -1*(8-calcsamp1);
									tempadd = 128;
								}
								else
								{
									calcsamp1 = calcsamp1-7;
									tempadd = 127;
								}
								calcsamp1 = (int)(((double)tempadd)+(128.0*((((double)calcsamp1)/7.0)*(((double)(lut[prevsamp].get(h+1)+1))/8.0))));
								firstcheck = true;
							}
							if(lut[i].get(k+1)==lut[prevsamp].get(h+1))
							{
								calcsamp2 = lut[prevsamp].get(h);
								if(calcsamp2<8)
								{
									calcsamp2 = -1*(8-calcsamp2);
									tempadd = 128;
								}
								else
								{
									calcsamp2 = calcsamp2-7;
									tempadd = 127;
								}
								calcsamp2 = (int)(((double)tempadd)+(128.0*((((double)calcsamp2)/7.0)*(((double)(lut[i].get(k+1)))/8.0))));
								secondcheck = true;
							}
							if(firstcheck==true && secondcheck==true)
							{
								if(calcsamp1<=calcsamp2 && currsamp<=calcsamp1 && calcsamp1<=prevsamp)//because the current amplitude is less than the previous one
								{
									output[0] = lut[i].get(k);
									output[1] = lut[prevsamp].get(h+1);
									return output;
								}
								if(calcsamp1>calcsamp2 && currsamp<=calcsamp2 && calcsamp2<=prevsamp)
								{
									output[0] = lut[prevsamp].get(h);
									output[1] = lut[i].get(k+1);
									return output;
								}
							}
						}
					}
				}
			}
		}
		output[0] = lut[currsamp].get(bestindex*2);
		output[1] = lut[currsamp].get((bestindex*2)+1);
		return output;
	}
	public static int quantize(int sample) //I got legacy mode working, happy days (:
	{
		if(sample>0 && sample <=17)
		{
			return 1;
		}
		if(sample>17 && sample<=35)
		{
			return 2;
		}
		if(sample>35 && sample<=53)
		{
			return 3;
		}
		if(sample>53 && sample<=72)
		{
			return 4;
		}
		if(sample>72 && sample<=90)
		{
			return 5;
		}
		if(sample>90 && sample<=108)
		{
			return 6;
		}
		if(sample>108 && sample<=127)
		{
			return 7;
		}
		if(sample>127 && sample<=145)
		{
			return 8;
		}
		if(sample>145 && sample<=163)
		{
			return 9;
		}
		if(sample>163 && sample<=181)
		{
			return 10;
		}
		if(sample>181 && sample<=200)
		{
			return 11;
		}
		if(sample>200 && sample<=218)
		{
			return 12;
		}
		if(sample>218 && sample<=236)
		{
			return 13;
		}
		return 14;
	}
	public static void main(String args[])
	{
		int system = Integer.parseInt(args[0]);
		int mode = Integer.parseInt(args[1]);
		int chans = Integer.parseInt(args[2]);
		int divider = Integer.parseInt(args[3]);
		String romsuff = "";
		String sysID = "";
		int[] makeargs = {system, mode, chans, divider};
		createSource(makeargs);
		if(system==0)
		{
			romsuff = ".gb";
		}
		else
		{
			romsuff = ".gbc";
			sysID = "-C";
		}
		createHeader(system);
		if(mode==1)
		{
			/*
			ArrayList<Integer>[] calcvals = new ArrayList[256];
			for(int i=0; i<calcvals.length; i++)
			{
				calcvals[i] = new ArrayList<Integer>();
			}
			double pos = 0;
			double neg = 0;
			int intpos = 0;
			int intneg = 0;
			for(int y=0; y<8; y++)
			{
				for(int x=1; x<8; x++)
				{
					pos = (((double)x)/7.0)*(((double)(y+1))/8.0);
					neg = -1.0*(((8.0-(double)x)/7.0)*(((double)(y+1))/8.0));
					intpos = ((int)(pos*128.0))+127;
					intneg = 128+((int)(neg*128.0));
					calcvals[intpos].add(x+7);
					calcvals[intpos].add(y);
					calcvals[intneg].add(x);
					calcvals[intneg].add(y);
				}
			}
			ArrayList<Integer> tempval = new ArrayList<Integer>();
			for(int i=0; i<128; i++) //these loops fill the gaps within the LUT array
			{
				if(calcvals[i].size()<=1)
				{
					for(int h=0; h<tempval.size(); h+=2)
					{
						calcvals[i].add(tempval.get(h));
						calcvals[i].add(tempval.get(h+1));
					}
				}
				for(int k=0; k<calcvals[i].size(); k+=2)
				{	
					if(calcvals[i].get(k)!=0)
					{	
						while(tempval.size()>0)
						{
							tempval.remove(0);
						}
						tempval.add(calcvals[i].get(k));
						tempval.add(calcvals[i].get(k+1));
					}
				}
			}
			for(int i=255; i>127; i--) //these loops fill the gaps within the LUT array
			{
				if(calcvals[i].size()<=1)
				{
					for(int h=0; h<tempval.size(); h+=2)
					{
						calcvals[i].add(tempval.get(h));
						calcvals[i].add(tempval.get(h+1));
					}
				}
				for(int k=0; k<calcvals[i].size(); k+=2)
				{	
					if(calcvals[i].get(k)!=0)
					{	
						while(tempval.size()>0)
						{
							tempval.remove(0);
						}
						tempval.add(calcvals[i].get(k));
						tempval.add(calcvals[i].get(k+1));
					}
				}
			}
			*/
			/*
			This section makes the fixed LUT.
			It sounds a lot better than using the dynamic amplitude algorithm,
			so I commented THAT out and never used it.
			Try it if you want, but it'll probably sound like garbage.
			Maybe add some println()s to see all of the values that the initial
			algorithm produces, and switch things around to see if you can make
			any improvements to the output quality.
			*/
			int uniquevals = 0;
			int pulse = 0;
			int outamp = 0;
			ArrayList<Integer>[] unsortedvals = new ArrayList[256];
			for(int i=0; i<256; i++)
			{
				unsortedvals[i] = new ArrayList<Integer>();
			}
				ArrayList<Integer> uvals = new ArrayList<Integer>();
			for(int m=1; m<=8; m++)
			{
				for(int p=1; p<=14; p++)
				{
					if(p<8)
					{
						pulse = p-8;
						outamp = (int)(128.0+((128.0/56.0)*((double)(pulse*m))));
					}
					else
					{
						pulse = p-7;
						outamp = (int)(127.0+((128.0/56.0)*((double)(pulse*m))));
					}
					if(uvals.indexOf(outamp)==-1)
					{
						uniquevals++;
						uvals.add(outamp);
					}
					unsortedvals[outamp].add(p);
					unsortedvals[outamp].add(m-1);
				}
			}
			ArrayList<Integer>[] variants = new ArrayList[uniquevals];
			ArrayList<Integer> uniqueamps = new ArrayList<Integer>();
			for(int i=0; i<uniquevals; i++)
			{
				variants[i] = new ArrayList<Integer>();
			}
			int min = 512;
			int mindex = 0;
			int vindex = 0;
			while(uvals.size()>0)
			{
				for(int k=0; k<uvals.size(); k++)
				{
					if(uvals.get(k)<min)
					{
						min=uvals.get(k);
						mindex = k;
					}
				}
				uvals.remove(mindex);
				mindex = 0;
				uniqueamps.add(min);
				variants[vindex].add(unsortedvals[min].get(0));
				variants[vindex].add(unsortedvals[min].get(1));
				vindex++;
				min = 512;
			}
			ArrayList<Integer>[] setlut = new ArrayList[256];
			for(int i=0; i<256; i++)
			{
				setlut[i] = new ArrayList<Integer>();
			}
			vindex = 57;
			int setindex = 255;
			while(setindex>=128)
			{
				if(setindex<=uniqueamps.get(vindex) && setindex>uniqueamps.get(vindex-1))
				{
					setlut[setindex].add(variants[vindex].get(0));
					setlut[setindex].add(variants[vindex].get(1));
					setindex--;
				}
				else
				{
					vindex--;
				}
			}
			vindex = 0;
			setindex = 0;
			while(setindex<=127)
			{
				if(setindex>=uniqueamps.get(vindex) && setindex<uniqueamps.get(vindex+1))
				{
					setlut[setindex].add(variants[vindex].get(0));
					setlut[setindex].add(variants[vindex].get(1));
					setindex++;
				}
				else
				{
					vindex++;
				}
			}
		try
		{	
			OutputStream fos = new FileOutputStream(("audio" + romsuff), true);
			InputStream fis = new FileInputStream("audio.raw");
			if(chans==2)
			{
				//int pleft = 128;
				//int pright = 128;
				int cleft = 128;
				int cright = 128;
				//int[] quantizedl = getBestValue(4, pleft, cleft, calcvals); //commented these out because not using dynamic algorithm
				//int[] quantizedr = getBestValue(4, pright, cright, calcvals);
				int[] quantizedl = {setlut[cleft].get(0), setlut[cleft].get(1)};
				int[] quantizedr = {setlut[cright].get(0), setlut[cright].get(1)};
				int[] outputs = new int[2];
				outputs[0]=(((quantizedl[0]&0x0F)<<4)|(quantizedr[0]&0x0F)); 
				outputs[1]=(((quantizedl[1]&0x0F)<<4)|(quantizedr[1]&0x0F));
				//fos.write(outputs[0]);
				//fos.write(outputs[1]);
				while((cleft=fis.read())!=-1)
				{
					cright=fis.read();
					//quantizedl = getBestValue(quantizedl[0], pleft, cleft, calcvals);
					quantizedl[0] = setlut[cleft].get(0);
					quantizedl[1] = setlut[cleft].get(1);
					//quantizedr = getBestValue(quantizedr[0], pright, cright, calcvals);
					quantizedr[0] = setlut[cright].get(0); 
					quantizedr[1] = setlut[cright].get(1);
					outputs[0] = (((quantizedl[0]&0x0F)<<4)|(quantizedr[0]&0x0F));
					outputs[1] = (((quantizedl[1]&0x0F)<<4)|(quantizedr[1]&0x0F));
					fos.write(outputs[0]);
					fos.write(outputs[1]);
					//pleft = cleft;
					//pright = cright;
				}
				fos.close();
				fis.close();
			}
			else
			{
				//int psamp = 128;
				int csamp = 128;
				//int[] quantized = getBestValue(4, psamp, csamp, calcvals);
				int[] quantized = {setlut[csamp].get(0), setlut[csamp].get(1)};
				int outputm = ((quantized[0]<<4)|(quantized[1]&0x0F));
				//fos.write(outputm);
				while((csamp=fis.read())!=-1)
				{
					//quantized = getBestValue(quantized[0], psamp, csamp, calcvals);
					quantized[0] = setlut[csamp].get(0);
					quantized[1] = setlut[csamp].get(1);
					outputm = ((quantized[0]<<4)|(quantized[1]&0x0F));
					fos.write(outputm);
					//psamp = csamp;
				}
				fos.close();
				fis.close();
			}
		}catch(IOException ex)
		{
			ex.printStackTrace();
		}
		}
		else
		{
			try{	
				OutputStream fos = new FileOutputStream(("audio" + romsuff), true);
				InputStream fis = new FileInputStream("audio.raw");
				int hsamp = 0;
				int lsamp = 0;
				int qhsamp = 0;
				int qlsamp = 0;
				int outsamp = 0;
				while((hsamp=fis.read())!=-1)
				{
					lsamp = (fis.read()&0xFF);
					qhsamp = quantize(hsamp);
					qlsamp = quantize(lsamp);
					outsamp = ((qhsamp<<4)|(qlsamp));
					fos.write(outsamp);
				}
				fos.close();
				fis.close();
			}catch(IOException ex)
			{
				ex.printStackTrace();
			}
		}
		try{	
				Process r = new ProcessBuilder().command("bash", "-c", "rgbfix -v -m 0x19 " + sysID + " -p 0 " + ("audio" + romsuff)).inheritIO().start();
				r.waitFor();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
	}
}