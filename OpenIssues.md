# Open Issues #

The language definition is still ongoing.
I have no solution yet for the following issues:

## A formalism to describe the language semantics ##
ACP-like axioms and Structural Operational Semantics do not seem to be suitable for
  * activation and deactivation effects, e.g. for executing code in directives and script calls
  * communication bodies that are no simple atomic actions
  * code fragments that are no simple atomic actions

## Hooks ##
The Node types should offer appropriate hooks so that the SubScript program execution may be tweaked.