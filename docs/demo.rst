=======================
Locator-free taxonomies
=======================

Why do we need a locator-free model?
====================================

XBRL taxonomies in their standard XML format are powerful representations of metadata for conforming XBRL
instances, even more so with the use of formula and table linkbases.

Yet the documents making up a taxonomy are hard to decouple from each other. The URI references in XLink locators,
XLink simple links and xs:import schemaLocation attributes lead to many circular dependencies across taxonomy
documents. Part of the reason is that taxonomy documents play 2 different roles: carry semantics, and contribute
to DTS (discoverable taxonomy set) discovery. To put it differently, standard XBRL taxonomies in XML format do not
promote local reasoning about their content.

This tight coupling among taxonomy documents has several consequences:

- To reduce memory footprint (and speed up loading) it would be nice to load a subset of the taxonomy documents where desired, but that's impossible without breaking closure under DTS discovery rules
- An in-memory model based on the document DOM trees would have the same tight coupling, again making it hard to reduce memory footprint while retaining closure under DTS discovery rules
- Adding an extension taxonomy to a base taxonomy would be more complex in memory than just "adding 2 taxonomies"
- Programmatically creating taxonomies (like test taxonomies for XBRL processing code) is harder than needed due to the complex "tangling" such software must do
- The same is true for editing (small) taxonomies by hand, which is now close to unthinkable
- XBRL taxonomy files are too hard to read in isolation, requiring tooling to make sense of them, even to developers of XBRL processing software

This may not all sound too shocking, but it's a matter of scale. For example, more easily creating one test taxonomy
programmatically is one thing, doing so for thousands of test taxonomies is quite another thing.

To combat this tight coupling and to promote local reasoning about taxonomy content, a different representation is needed
(at least in memory, but preferably also in file format).

Can we do so with a format close enough to the standard taxonomy format, in order to facilitate lossless roundtripping
between standard taxonomies and the alternative taxonomy format?

It turns out that this is the case. An alternative XML taxonomy representation is possible such that:

- The tight coupling is gone
- There is still a one-to-one correspondence between individual standard taxonomy documents and their counterparts

The locator-free model
======================

Networks of relationships
=========================

Creating locator-free taxonomies programmatically
=================================================

Entrypoints
===========

Extension taxonomies
====================

Conclusion
==========

