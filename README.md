# Word Count MapReduce Application

This project implements a word count application using Hadoop's MapReduce framework. It processes Project Gutenberg text files, analyzing word frequencies with and without stop word removal.

**Code Functionality:**

The Java code implements the MapReduce algorithm, including mapping, combining, and reducing phases. It utilizes Hadoop's data types (Text, IntWritable) and optimizes performance with a combiner.  A Python script downloads the dataset. Stop words are read from a file.

**Running the Code:**

1.  Set up a Hadoop environment.
2.  Download the dataset using the provided Python script.
3.  Upload the text files to HDFS.
4.  Compile the Java code: `javac -classpath $(hadoop classpath) WordCount.java`
5.  Create a JAR file: `jar cf wc.jar *.class`
6.  Run the application (with/without stopwords):
    *   With stopwords: `hadoop jar wc.jar WordCount /books /output_with_stopwords`
    *   Without stopwords: `hadoop jar wc.jar WordCount /books /output_filtered stopwords.txt`
7.  Retrieve results from HDFS: `hdfs dfs -cat /output_*/part* | sort -k2nr | head -25`
