# C# Transformation Features - Test Index

This directory contains individual test files for each C# to Java transformation feature implemented in RefactoringMiner's C# extension.

## Quick Start

Run all tests:
```bash
./run-tests.sh
```

Or manually:
```bash
javac -cp build/libs/RM-fat.jar CSharpTransformationTestSuite.java
java -cp build/libs/RM-fat.jar:. CSharpTransformationTestSuite
```

## Test Files Overview

### Basic Language Constructs

**01_field_declarations.cs**
- Public, private, protected field declarations
- Static and readonly fields
- Constant declarations
- Backing field patterns with underscore prefix

**02_lambda_properties.cs**
- Expression-bodied property getters (get =>)
- Expression-bodied property setters (set =>)
- Computed property values
- Null-coalescing in properties

**03_block_properties.cs**
- Properties with explicit getter bodies
- Properties with explicit setter bodies
- Validation logic in setters
- Side effects in property accessors

**04_auto_properties.cs**
- Simple auto-properties (get; set;)
- Auto-properties with initial values
- Read-only auto-properties
- Private and protected setters

**05_events.cs**
- Event declarations without arguments
- Events with single argument (Action<T>)
- Events with multiple arguments
- EventHandler pattern
- Event invocation with null-conditional operator

**06_constructors.cs**
- Default parameterless constructors
- Single parameter constructors
- Multiple parameter constructors
- Constructor overloading
- Private constructors for factory patterns

**07_methods.cs**
- Void methods without parameters
- Methods with return values
- Methods with multiple parameters
- Public, private, protected methods
- Static methods
- Methods with multiple statements

**08_type_conversions.cs**
- Primitive type conversions (string, bool, int, long, float, double, char, byte, short)
- Object type conversion
- Generic collection types (List<T>, Dictionary<K,V>)
- Type conversions in method signatures

### Object-Oriented Features

**09_sealed_classes.cs**
- Sealed class declaration (sealed -> final)
- Multiple sealed classes in same file
- Sealed classes with properties and methods

**10_abstract_classes.cs**
- Abstract class declarations
- Abstract method declarations
- Concrete methods in abstract classes
- Multiple abstract classes

**11_inheritance.cs**
- Single inheritance (extends)
- Multiple level inheritance
- Method overriding
- Base class constructors

**12_interfaces.cs**
- Interface declarations
- Single interface implementation
- Multiple interface implementation
- Interface methods

### Advanced Type Features

**13_nullable_types.cs**
- Nullable value types (int?, bool?, double?, long?, float?)
- Nullable type parameters in methods
- Nullable return types
- HasValue and Value property access
- Null-coalescing with nullable types

### Control Flow

**14_exception_handling.cs**
- Basic try-catch blocks
- Try-catch-finally blocks
- Multiple catch blocks
- Throw statements
- Conditional throw statements
- Nested try-catch blocks

**15_switch_statements.cs**
- Switch with integer values
- Switch with string values
- Fall-through cases
- Switch with return statements
- Multiple statements per case
- Default case handling

**16_loops.cs**
- Basic while loops
- While loops with complex conditions
- Do-while loops
- For loops with initialization, condition, increment
- For loops with custom step values
- Nested loops
- Break statements in loops
- Continue statements in loops

**17_control_flow.cs**
- Basic if statements
- If-else statements
- If-else-if chains
- Nested if statements
- Complex boolean conditions
- Break and continue usage
- Early return patterns
- Ternary operator (?:)

### Resource Management and Operators

**18_using_statements.cs**
- Basic using statements with StreamReader
- Using with StreamWriter
- Using with FileStream
- Nested using statements
- Multiple sequential using statements
- Using with exception handling

**19_null_operators.cs**
- Null-coalescing operator (??)
- Null-coalescing assignment (??=)
- Chained null-coalescing
- Null-conditional operator (?.)
- Null-conditional with method calls
- Null-conditional with indexers
- Combined null-conditional and null-coalescing

**20_attributes.cs**
- Class-level attributes
- Field-level attributes
- Method-level attributes
- Property-level attributes
- Parameter-level attributes
- Custom attribute definitions
- AttributeUsage patterns

## Test Execution Results

Last test run: All 20 tests passed (100%)

### Performance Metrics
- Total execution time: 923 ms
- Average time per test: 46 ms
- Total source lines: 1391
- Types created: 27
- Fields transformed: 80
- Methods transformed: 158

## Adding New Tests

To add a new feature test:

1. Create file in `tests/csharp-features/` with pattern: `##_feature_name.cs`
2. Use namespace `TestFeatures`
3. Include XML documentation describing the test
4. Add comprehensive test cases for the feature
5. Run test suite to validate

## Test Validation

Each test validates:
1. srcML parsing succeeds
2. XML processing completes
3. Java code generation succeeds
4. CompilationUnit creation succeeds
5. Correct number of types created
6. Expected fields and methods present

## Troubleshooting

If tests fail:
1. Check srcML installation: `which srcml`
2. Verify Java version: `java -version`
3. Rebuild project: `./gradlew clean shadowJar`
4. Check test file syntax for C# errors
5. Review processor logs for specific errors

## Documentation

See `tests/README.md` for comprehensive documentation including:
- Detailed test design principles
- CI/CD integration
- Performance benchmarking
- Issue reporting guidelines
