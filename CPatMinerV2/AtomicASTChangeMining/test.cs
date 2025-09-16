using System;
using System.Reflection;

delegate int NumberChanger(int n);
public struct Point
{
    public int X { get; set; }
    public int Y { get; set; }
}

namespace HelloWorld
{
public enum DaysOfWeek
    {
        Sunday,
        Monday,
        Tuesday,
        Wednesday,
        Thursday,
        Friday,
        Saturday
    }

    interface IContravariant<in A> { }
    class Conversion<out A> { }
    class GenericList<T> where T : Employee { }
    class Foo<T> where T : new() { }
    class Foo<T> where T : class {
    Foo<T> foo<X>(X x) where X : struct {
     var scoreQuery =
              from score in scores
              join prod in products on category.ID equals prod.CategoryID
              let words = word.ToLower()
              where score > 80 || w[0] == 'e'
              orderby a descending
              group student by student.Last into g
              select score;
          }
     }

    [Author("Jane Programmer", Version = 2), IsTested()]
     class Hello : SuperClass, Superinterface {

     private string privateField;
     int lol;
     public event SampleEventHandler SampleEvent;
     public double Hours {
         get;
         set { seconds = value * 3600; }
     }


     public Hello(test<lol> x, int y)
         {
             (Point lhs, Point rhs) => lhs.x < rhs.y;
             x => { return x + 1; };

             Foo<test> myVar = Level.Medium;
             Type type = typeof(int);
             int i = sizeof(x);
             checked {
                 int i3 = 2147483647 + ten;
                 Console.WriteLine(i3);
             }
            ;
             try
                     {
                         throw new DivideByZeroException("oh no.");
                     }
                     catch (DivideByZeroException ex)
                     {
                         Console.WriteLine("lol" + ex.Message);
                     }
                     finally
                     {
                         Console.WriteLine("This block will always be executed.");
                     }
             using (StreamReader sr = new StreamReader("TestFile.txt")) {
             //do smthg
             }
             fixed (int* p = &pt.x) {
                 //*p = 1;
             }
         }
         ~Hello()
             {
                 // Cleanup code
             }
        public static unsafe void main()
        {
            Console.WriteLine( a + 5 * 33, new MyClass("hi",55));
            Class1 varrr = (String )a.get(lol) + 5;
            unsafe {
            a = a+ b
            }
            lock (thisLock) {
            a = a+ b
            }

        }
        int test(int a,  int b){
            int k;
            a = k ? x : y;
            a = 2;
            a++;
            a+= 5;
            int t = 4 + 33 / 18;
            t = 6 - 7 * 8 / a;
            int time = 22;
            while (time < 5)
            {
              Console.WriteLine(i);
            }
            do {
                y = test( x );
            } while ( x > 0 );

        for (int i = 1; i <= 5; i++)
        {
            Console.WriteLine(i);
            if (True)
                continue;
        }
        foreach (int number in numbers)
                {
                    Console.WriteLine(number);
                }
            if (time < 10 && b == 2)
            {
              Console.WriteLine("Good morning.");
            }
            else if (time < 20)
            {
              Console.WriteLine("Good day.");
            }
            else
            {
              goto stop;
            }
            switch (day)
            {
              case 1:
                Console.WriteLine("Monday");
                x = x+1;
                break;
              case 2:
                Console.WriteLine("Tuesday");
                goto case 1;
              default:
                break;
            }
            return t;
        }
    }
    abstract class testttAbstract {
            public abstract void doWork();


        }
    public interface Superinterface
            {
                double CalculateArea();
            }
}