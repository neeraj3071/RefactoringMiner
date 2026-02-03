using System;
using System.Collections.Generic;
using System.Linq;

public class ComplexTest {
    public string Name { get; set; }
    public int? Age { get; set; }

    public void Run(List<string> items) {
        var filtered = items.Where(s => s?.Length > 1)
                            .Select(s => s.ToUpper())
                            .ToList();

        foreach (var item in filtered) {
            Console.WriteLine(item ?? "empty");
        }

        var first = items.FirstOrDefault(x => x is not null && x.Contains("a"));
        var tuple = (x: 1, y: 2);
        var (a, b) = tuple;

        var dict = new Dictionary<string, int> { ["a"] = 1 };
        dict.TryGetValue("a", out var value);

        Func<int, int> f = x => x * 2;
        var result = f(21);

        var query = from s in items
                    where s.StartsWith("a")
                    select s;

        var count = query.Count();
    }
}
