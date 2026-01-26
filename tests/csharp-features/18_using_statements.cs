namespace TestFeatures
{
    using System;
    using System.IO;
    
    /// <summary>
    /// Test case for using statements (resource management).
    /// Tests transformation of C# using statements to Java try-with-resources.
    /// </summary>
    public class UsingStatements
    {
        // Basic using statement with StreamReader
        public void BasicUsing()
        {
            using (StreamReader reader = new StreamReader("file.txt"))
            {
                string line = reader.ReadLine();
                Console.WriteLine(line);
            }
        }
        
        // Using statement with StreamWriter
        public void UsingWithWriter()
        {
            using (StreamWriter writer = new StreamWriter("output.txt"))
            {
                writer.WriteLine("Hello World");
                writer.WriteLine("Second line");
            }
        }
        
        // Using statement with FileStream
        public void UsingWithFileStream()
        {
            using (FileStream fs = new FileStream("data.bin", FileMode.Open))
            {
                byte[] buffer = new byte[1024];
                int bytesRead = fs.Read(buffer, 0, buffer.Length);
                Console.WriteLine("Read " + bytesRead + " bytes");
            }
        }
        
        // Nested using statements
        public void NestedUsing()
        {
            using (StreamReader reader = new StreamReader("input.txt"))
            {
                using (StreamWriter writer = new StreamWriter("output.txt"))
                {
                    string line;
                    while ((line = reader.ReadLine()) != null)
                    {
                        writer.WriteLine(line.ToUpper());
                    }
                }
            }
        }
        
        // Multiple using statements (C# 8.0 style - sequential)
        public void MultipleUsing()
        {
            using (StreamReader reader1 = new StreamReader("file1.txt"))
            using (StreamReader reader2 = new StreamReader("file2.txt"))
            {
                string line1 = reader1.ReadLine();
                string line2 = reader2.ReadLine();
                Console.WriteLine(line1 + " - " + line2);
            }
        }
        
        // Using with exception handling
        public void UsingWithTryCatch()
        {
            try
            {
                using (StreamReader reader = new StreamReader("file.txt"))
                {
                    string content = reader.ReadToEnd();
                    Console.WriteLine(content);
                }
            }
            catch (IOException e)
            {
                Console.WriteLine("IO Error: " + e.Message);
            }
        }
    }
}
