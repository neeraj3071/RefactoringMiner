/**
 * Debug utilities for analyzing the C# to Java transformation flow in RefactoringMiner
 * 
 * <p>This package contains standalone runnable classes that help you trace and debug
 * the complete flow of C# source code transformation to refactoring detection.</p>
 * 
 * <h2>Available Debug Runners:</h2>
 * <ul>
 *   <li>{@link org.refactoringminer.csharp.debug.ASTFlowDebugger} - 
 *       Trace complete flow from C# source to UMLModel using CPatMiner</li>
 * </ul>
 * 
 * <h2>How to Use:</h2>
 * <ol>
 *   <li>Open the debug runner class you want to use</li>
 *   <li>Set breakpoints at the indicated locations in the code</li>
 *   <li>Run the class in debug mode from VS Code or your IDE</li>
 *   <li>Step through the code to understand the transformation flow</li>
 * </ol>
 * 
 * <h2>Key Breakpoint Locations:</h2>
 * <ul>
 *   <li>{@code CPatMinerExecutor.transformCSharpToJavaAST()} - CPatMiner transformation entry</li>
 *   <li>{@code CPatMinerExecutor.tryCPatMinerTransformation()} - CPatMiner execution</li>
 *   <li>{@code CSharpUMLModelASTReader.processFilesWithCPatMiner()} - File processing</li>
 *   <li>{@code UMLModelASTReader.processJavaFileContents()} - Java parsing</li>
 *   <li>{@code UMLModelASTReader.processCompilationUnit()} - UMLModel creation</li>
 * </ul>
 */
package org.refactoringminer.csharp.debug;
