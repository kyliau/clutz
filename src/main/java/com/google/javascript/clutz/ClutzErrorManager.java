package com.google.javascript.clutz;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.MessageFormatter;
import com.google.javascript.jscomp.PrintStreamErrorManager;
import java.io.PrintStream;

/**
 * An error manager that delays reporting through {@link #doGenerateReport()}, allows failing fast,
 * and that adds special, Clutz-specific errors.
 */
final class ClutzErrorManager extends PrintStreamErrorManager {
  private final boolean debug;
  private boolean partialCompilation;

  private boolean hasEmittedMissingTypesExplanation = false;

  ClutzErrorManager(PrintStream stream, MessageFormatter formatter, boolean debug,
      boolean partialCompilation) {
    super(formatter, stream);
    this.debug = debug;
    this.partialCompilation = partialCompilation;
  }

  @Override
  public void report(CheckLevel level, JSError error) {
    // Ignore warnings in non-debug mode.
    if (!debug && level == CheckLevel.WARNING) return;

    if (partialCompilation && DiagnosticGroups.PARTIAL_COMPILE.matches(error)) {
      return;  // Ignore the error completely.
    }

    if (!hasEmittedMissingTypesExplanation) {
      // Prepend an error that hints at missing externs/dependencies.
      hasEmittedMissingTypesExplanation = true;
      // Leave out the location on purpose, the specific places of missing types are reported from
      // the original message; without a location this error sorts first, so that it is seen
      // first.
      this.report(CheckLevel.ERROR, JSError.make(DeclarationGenerator.CLUTZ_MISSING_TYPES));
      // Fall through, still report the actual error below.
    }
    super.report(level, error);
  }

  @Override
  public void generateReport() {
    // Don't do anything - only generate a report in {@link #doGenerateReport} below after both the
    // compiler and Clutz itself have run.
    // TODO(martinprobst): Clutz should probable be refactored into a CompilerPass, at which point
    // this method would be obsolete.
  }

  void doGenerateReport() {
    super.generateReport();
  }
}
