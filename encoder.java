/*
Gameboy Audio Encoder 2.0, Java encoder build 2.3, 2/6/2021
Made by Jackson Shelton
See README for OS-dependent(?) changes for command line and
how to compile for your system.

I plan to update the ideal amplitude choosing algorithm in
the near future; it's a lot better than my old one (which
barely worked,) but it still has a small amount of
clipping/distortion issues because of the amplitude loading
format.
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
				mainsource = "monogb.asm";
			}
			else
			{
				mainsource = "stereogb.asm";
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
		output[0] = lut[currsamp].get(bestindex*2);
		output[1] = lut[currsamp].get((bestindex*2)+1);
		return output;
	}
	public static int quantize(int sample)
	{
		if(sample>0 && sample <=16)
		{
			return 1;
		}
		if(sample>16 && sample<=33)
		{
			return 2;
		}
		if(sample>33 && sample<=50)
		{
			return 3;
		}
		if(sample>51 && sample<=67)
		{
			return 4;
		}
		if(sample>67 && sample<=84)
		{
			return 5;
		}
		if(sample>84 && sample<=101)
		{
			return 6;
		}
		if(sample>101 && sample<=118)
		{
			return 7;
		}
		if(sample>118 && sample<=135)
		{
			return 8;
		}
		if(sample>135 && sample<=152)
		{
			return 9;
		}
		if(sample>152 && sample<=169)
		{
			return 10;
		}
		if(sample>169 && sample<=186)
		{
			return 11;
		}
		if(sample>186 && sample<=203)
		{
			return 12;
		}
		if(sample>203 && sample<=220)
		{
			return 13;
		}
		if(sample>220 && sample<=237)
		{
			return 14;
		}
		return 15;
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
		if(system==1)
		{
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
		try
		{	
			OutputStream fos = new FileOutputStream(("audio" + romsuff), true);
			InputStream fis = new FileInputStream("audio.raw");
			if(chans==2)
			{
				int pleft = 128;
				int pright = 128;
				int cleft = 128;
				int cright = 128;
				int[] quantizedl = getBestValue(4, pleft, cleft, calcvals);
				int[] quantizedr = getBestValue(4, pright, cright, calcvals);
				int[] outputs = new int[2];
				outputs[0]=(((quantizedl[0]&0x0F)<<4)|(quantizedr[0]&0x0F)); 
				outputs[1]=(((quantizedl[1]&0x0F)<<4)|(quantizedr[1]&0x0F));
				fos.write(outputs[0]);
				fos.write(outputs[1]);
				while((cleft=fis.read())!=-1)
				{
					cright=fis.read();
					quantizedl = getBestValue(quantizedl[0], pleft, cleft, calcvals);
					quantizedr = getBestValue(quantizedr[0], pright, cright, calcvals);
					outputs[0] = (((quantizedl[0]&0x0F)<<4)|(quantizedr[0]&0x0F));
					outputs[1] = (((quantizedl[1]&0x0F)<<4)|(quantizedr[1]&0x0F));
					fos.write(outputs[0]);
					fos.write(outputs[1]);
					pleft = cleft;
					pright = cright;
				}
				fos.close();
				fis.close();
			}
			else
			{
				int psamp = 128;
				int csamp = 128;
				int[] quantized = getBestValue(4, psamp, csamp, calcvals);
				int outputm = ((quantized[0]<<4)|(quantized[1]&0x0F));
				fos.write(outputm);
				while((csamp=fis.read())!=-1)
				{
					quantized = getBestValue(quantized[0], psamp, csamp, calcvals);
					outputm = ((quantized[0]<<4)|(quantized[1]&0x0F));
					fos.write(outputm);
					psamp = csamp;
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