package edu.columbia.cvml.galleria.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;

public class FileOperation
{

	public static boolean writeFileToInternalStorage(Context ctx, String fileName, String data)
	{
		BufferedWriter writer = null;
		try
		{
			writer = new BufferedWriter(new OutputStreamWriter(ctx.openFileOutput(fileName, Context.MODE_APPEND)));
			writer.write(data);
			return true;
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		} 
		finally
		{
			if (writer != null)
			{
				try 
				{
					writer.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public static String readFileFromInternalStorage(Context ctx, String fileName)
	{
		String eol = "\r\n";
		BufferedReader input = null;
		try
		{
			input = new BufferedReader(new InputStreamReader(ctx.openFileInput(fileName)));
			String line;
			StringBuffer buffer = new StringBuffer();
			while ((line = input.readLine()) != null)
			{
				buffer.append(line + eol);
			}
			return buffer.toString();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		} 
		finally
		{
			if (input != null)
			{
				try 
				{
					input.close();
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	} 
}
