import java.io.BufferedReader;
import java.io.IOException; // Add this import for IOException
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class WordCount {

  public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    private Set<String> stopwords = new HashSet<>();

    // Method to read stopwords from the file on HDFS
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      Configuration conf = context.getConfiguration();
      Path stopwordFilePath = new Path(conf.get("stopwords.file.path")); // Read path from configuration

      // Reading the stopwords file from HDFS
      FileSystem fs = FileSystem.get(URI.create(stopwordFilePath.toString()), conf);
      BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(stopwordFilePath)));

      String line;
      while ((line = reader.readLine()) != null) {
        stopwords.add(line.trim().toLowerCase()); // Add stopwords to set
      }
      reader.close();
    }

    // Map method
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString().replaceAll("[^a-zA-Z]", " ").toLowerCase());
      while (itr.hasMoreTokens()) {
        String token = itr.nextToken();
        if (!stopwords.contains(token)) { // Exclude stopwords
          word.set(token);
          context.write(word, one);
        }
      }
    }
  }

  public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    private IntWritable result = new IntWritable();

    // Reduce method
    public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

    if (otherArgs.length < 3) {
      System.err.println("Usage: wordcount <in> <out> <stopwords.txt>");
      System.exit(2);
    }

    // Set the stopwords file path in the configuration
    conf.set("stopwords.file.path", otherArgs[2]);

    Job job = Job.getInstance(conf, "word count with stopwords");
    job.setJarByClass(WordCount.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);

    FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
    FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}

