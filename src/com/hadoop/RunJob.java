package com.hadoop;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class RunJob {

	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	static class MapperJob extends Mapper<LongWritable, Text, keyPair, Text>
	{

		@Override
		protected void map(LongWritable key, Text value, 
				Context context)
				throws IOException, InterruptedException {

			//������ȡ��һ������
			String line = value.toString();
			//ÿһ���������Ʊ����ָ������Ҫ�ָ�
			String[] ss = line.split("\t");
			
			System.out.println("value=" + value.toString());
			System.out.println("ss.length=" + ss.length);
			//ֻ������������������
			if (ss.length == 2) {
				try {
					//�������
					Date date = sdf.parse(ss[0]);
					Calendar c = Calendar.getInstance();
					c.setTime(date);
					//c.get(1)�������
					int year = c.get(1);
					System.out.println("year=" + year);
					//�����¶�
					String t = ss[1].substring(0, ss[1].indexOf("C"));
					System.out.println("t=" + t);
					
					//�������϶����ֵkeyPair
					keyPair k = new keyPair();
					k.setYear(year);
					k.setTemperature(Integer.parseInt(t));
					
					Text t1 = new Text(t);
					System.out.println("t1=" + t1);
					//mapperд���
					context.write(k, t1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	
	static class ReducerJob extends Reducer<keyPair, Text, keyPair, Text>
	{
		
		protected void reduce(keyPair key, Iterable<Text> value, Context context)
				throws IOException, InterruptedException {
			//ֱ�����
			for(Text v: value)
			{
				context.write(key, v);
			}
		}
		
	}
	
	public static void main(String[] args) {
		Configuration conf = new Configuration();
		try
		{
			Job job = new Job(conf);
			job.setJobName("year_temperature");
			job.setJarByClass(RunJob.class);
			job.setMapperClass(MapperJob.class);
			job.setReducerClass(ReducerJob.class);
			job.setMapOutputKeyClass(keyPair.class);
			job.setMapOutputValueClass(Text.class);
			
			job.setNumReduceTasks(3);
			job.setPartitionerClass(Partition.class);
			job.setSortComparatorClass(Sort.class);
			job.setGroupingComparatorClass(Group.class);
			
			//��������ļ�·��
			FileInputFormat.addInputPath(job, new Path("/usr/local/hadooptempdata/input/year-temp/"));
			FileOutputFormat.setOutputPath(job, new Path("/usr/local/hadooptempdata/output/year-temp/"));
			System.exit(job.waitForCompletion(true)? 0 : 1);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}