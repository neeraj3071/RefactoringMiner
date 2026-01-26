package TestFeatures;

public class UsingStatements {
    public void BasicUsing() {
        try (StreamReader reader = new StreamReader("file.txt")) {
            String line = reader.ReadLine();
            System.out.println(line);
        }
    }
    public void UsingWithWriter() {
        try (StreamWriter writer = new StreamWriter("output.txt")) {
            writer.WriteLine("Hello World");
            writer.WriteLine("Second line");
        }
    }
    public void UsingWithFileStream() {
        try (FileStream fs = new FileStream("data.bin", FileMode.Open)) {
            byte[] buffer = new byte[1024];
            int bytesRead = fs.Read(buffer, 0, buffer.Length);
            System.out.println("Read " + bytesRead + " bytes");
        }
    }
    public void NestedUsing() {
        try (StreamReader reader = new StreamReader("input.txt")) {
            try (StreamWriter writer = new StreamWriter("output.txt")) {
                String line;
                while (((line = reader.readLine()) != null)) {
                    writer.println(line.toUpperCase());
                }
            }
        }
    }
    public void MultipleUsing() {
        try (StreamReader reader1 = new StreamReader("file1.txt")) {
            String line1 = reader1.ReadLine();
            String line2 = reader2.ReadLine();
            System.out.println(line1 + " - " + line2);
        }
    }
    public void UsingWithTryCatch() {
        try {
            try (StreamReader reader = new StreamReader("file.txt")) {
                String content = reader.ReadToEnd();
                System.out.println(content);
            }
        } catch (Exception e) {
            System.out.println("IO Error: " + e.getMessage());
        }
    }
}
