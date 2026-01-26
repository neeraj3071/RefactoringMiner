package TransformationExample;

import java.util.List;

public class UpdateMethod
{
    public void Update(List<String> items, String text)
    {
        for (String item : items)
            System.out.println((item != null ? item : text));
    }
}
