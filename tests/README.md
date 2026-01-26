# C# to Java Transformation Test Suite

Comprehensive end-to-end testing framework for validating all C# to Java transformation features in RefactoringMiner's C# extension.

## Test Organization

All test files are located in `tests/csharp-features/` with each file testing a specific feature independently.

### Test Files

| File | Feature | Description |
|------|---------|-------------|
| 01_field_declarations.cs | Field Declarations | Public, private, protected, static, readonly, const fields |
| 02_lambda_properties.cs | Lambda Properties | Expression-bodied properties (get =>, set =>) |
| 03_block_properties.cs | Block Properties | Properties with explicit getter/setter bodies |
| 04_auto_properties.cs | Auto Properties | Auto-implemented properties (get; set;) |
| 05_events.cs | Events | C# events transformed to Java listener pattern |
| 06_constructors.cs | Constructors | Default, parameterized, overloaded constructors |
| 07_methods.cs | Methods | Public, private, protected, static methods |
| 08_type_conversions.cs | Type Conversions | C# primitives to Java equivalents (string->String, bool->boolean) |
| 09_sealed_classes.cs | Sealed Classes | C# sealed keyword to Java final keyword |
| 10_abstract_classes.cs | Abstract Classes | Abstract classes with abstract and concrete members |
| 11_inheritance.cs | Inheritance | Class inheritance using extends keyword |
| 12_interfaces.cs | Interfaces | Interface implementation using implements keyword |
| 13_nullable_types.cs | Nullable Types | Nullable value types (int?, bool?) to wrapper types |
| 14_exception_handling.cs | Exception Handling | Try-catch-finally blocks and throw statements |
| 15_switch_statements.cs | Switch Statements | Switch cases with various patterns |
| 16_loops.cs | Loops | While, do-while, for loops with break/continue |
| 17_control_flow.cs | Control Flow | If-else statements, ternary operators |
| 18_using_statements.cs | Using Statements | Resource management to try-with-resources |
| 19_null_operators.cs | Null Operators | Null-coalescing (??) and null-conditional (?.) operators |
| 20_attributes.cs | Attributes | C# attributes to Java annotations |

## Running Tests

### Compile and Run All Tests

```bash
javac -cp build/libs/RM-fat.jar CSharpTransformationTestSuite.java
java -cp build/libs/RM-fat.jar:. CSharpTransformationTestSuite
```

### Run Individual Test

```bash
java -cp build/libs/RM-fat.jar org.refactoringminer.csharp.SrcMLBasedCSharpProcessor \
  tests/csharp-features/01_field_declarations.cs
```

## Test Output

The test suite produces comprehensive output including:

### Per-Test Metrics
- CompilationUnit creation status (PASS/FAIL)
- Number of types created
- Number of fields transformed
- Number of methods transformed
- Execution time in milliseconds

### Aggregate Statistics
- Total tests run
- Pass/fail percentages
- Total execution time
- Average time per test
- Total source lines processed
- Aggregate transformation counts

### Example Output

```
================================================================================
C# to Java Transformation Test Suite
RefactoringMiner C# Extension - Comprehensive Feature Validation
================================================================================

Found 20 test files

================================================================================

[1] Testing: 01 field declarations
--------------------------------------------------------------------------------
    Transforming C# to Java AST...
    [PASS] CompilationUnit created successfully
    Types: 1
    Fields: 15
    Methods: 0
    Execution time: 245 ms

[2] Testing: 02 lambda properties
--------------------------------------------------------------------------------
    Transforming C# to Java AST...
    [PASS] CompilationUnit created successfully
    Types: 1
    Fields: 7
    Methods: 14
    Execution time: 198 ms

...

================================================================================
TEST SUMMARY
================================================================================

Total Tests:  20
Passed:       20 (100%)
Failed:       0 (0%)

Aggregate Statistics:
  Total execution time:  4523 ms
  Average time per test: 226 ms
  Total source lines:    1543
  Types created:         42
  Fields transformed:    156
  Methods transformed:   248

================================================================================
ALL TESTS PASSED
================================================================================
```

## Test Design Principles

### 1. Independence
Each test file focuses on a single feature and can be run independently without dependencies on other tests.

### 2. Comprehensive Coverage
Tests cover both common patterns and edge cases for each feature:
- Basic usage
- Complex scenarios
- Multiple variations
- Nested structures
- Error conditions

### 3. Professional Quality
- Detailed XML documentation for each test class
- Descriptive variable and method names
- Clear comments explaining test intent
- Realistic code patterns matching production usage

### 4. Verifiable Results
Each test produces measurable outcomes:
- CompilationUnit creation success/failure
- Type, field, and method counts
- Execution timing
- Error messages for debugging

## Extending the Test Suite

To add a new test:

1. Create a new .cs file in `tests/csharp-features/`
2. Follow the naming convention: `##_feature_name.cs`
3. Include XML documentation with test description
4. Use the TestFeatures namespace
5. Run the test suite to validate

Example:

```csharp
namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Test case for [feature name].
    /// Tests transformation of [C# feature] to [Java equivalent].
    /// </summary>
    public class NewFeatureTest
    {
        // Test implementation
    }
}
```

## Validation Criteria

A test passes if:
1. C# file is successfully parsed by srcML
2. XML is correctly processed
3. Java code is generated
4. CompilationUnit is created without errors
5. Expected types, fields, and methods are present

## Integration with CI/CD

The test suite returns appropriate exit codes:
- Exit 0: All tests passed
- Exit 1: One or more tests failed

This enables integration with continuous integration systems:

```bash
./gradlew shadowJar && \
javac -cp build/libs/RM-fat.jar CSharpTransformationTestSuite.java && \
java -cp build/libs/RM-fat.jar:. CSharpTransformationTestSuite
```

## Troubleshooting

### srcML Not Found
Ensure srcML is installed and in PATH:
```bash
which srcml  # Should show /opt/homebrew/bin/srcml or similar
```

### CompilationUnit Creation Fails
Check Java version compatibility. The processor uses Java 11 AST parser.

### Test Files Not Found
Verify the test directory exists and contains .cs files:
```bash
ls -la tests/csharp-features/
```

## Test Maintenance

Tests should be updated when:
- New C# features are added to the processor
- Transformation logic changes
- Bug fixes affect output structure
- Java AST parser version changes

## Performance Benchmarking

The test suite tracks execution time for each test, enabling performance regression detection:

```bash
# Run multiple times and compare results
for i in {1..5}; do
  java -cp build/libs/RM-fat.jar:. CSharpTransformationTestSuite | grep "Average time"
done
```

