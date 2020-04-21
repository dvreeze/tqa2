=======================
Locator-free taxonomies
=======================

Why do we need a locator-free model?
====================================

XBRL taxonomies in their standard XML format are powerful representations of metadata for XBRL instances
conforming to them, even more so if these taxonomies contain formula and table linkbases.

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

We dubbed this model the locator-free taxonomy model.

The locator-free model
======================

Locator-free taxonomies are characterized as follows:

- They are XML files, just like standard taxonomy files
- Like standard taxonomies, they have 2 kinds of files: schema files and "linkbase" files
- Each schema file in the standard taxonomy has an equally named locator-free counterpart
- SImilarly, each linkbase file in the standard taxonomy has an equally named locator-free counterpart
- Schema documents in the locator-free model are schema documents according to XML Schema (like their standard XBRL schema counterparts)
- Linkbase documents in the locator-free model are *not* XBRL linkbases
- Locator-free linkbases *do* use XLink
- Yet XLink in the locator-free model is restricted to *XLink without locators and without simple links*
- Instead of XLink locators there are *taxonomy element keys*, which are XLink resources
- For example, *concept keys* contain the QName (of schema type xs:QName) of the concept they refer to
- This reminds somewhat of table and formula linkbases in standard taxonomies, which also lean heavily on XLink resources and much less so on XLink locators
- Schema files in the locator-free model contain no xs:include elements, and no schemaLocation attributes on xs:import elements
- That is, unless they act as "entrypoints" in the locator-free model, but more about that later
- Other than "entrypoints", all taxonomy files are "standalone" in that they contribute nothing to the DTS other than themselves
- After all, locator-free linkbases and "standalone" schemas contain no URI references (not even xbrldt:typedDomainRef attributes)

So locators have been replaced by (mostly) semantic keys, and combining documents into a set of taxonomy documents is
a bit like "database joins". The semantic keys do not care about the location of the taxonomy element referred to.

So, what does that mean for the XML elements used in the locator-free model?

For "standalone" schemas:

- They are pretty much the same as the standard XBRL counterpart
- The schemaLocation attribute in xs:import elements disappears
- The xbrldt:typedDomainRef attribute (in xs:emement) becomes cxbrldt:typedDomainKey, which is typed xs:QName

For linkbases:

- The outer element becomes clink:linkbase (so another namespace, so it is not an XBRL linkbase)

For presentation, definition and calculation extended links:

- The names are clink:presentationLink etc. (so another namespace, so it is not an XBRL standard link)
- The arc names are clink:presentationArc etc. (ater all, not a standard arc according to XBRL)
- The locators have been replaced by ckey:conceptKey elements, which are XLink resources
- So the (extended) links contain only XLink arcs and concept keys, which are XLink resources
- There may be clink:roleRef and clink:arcroleRef elements, which are not XLink simple links, and contain no href attribute

For (standard) label and reference extended links:

- The names are clink:labelLink etc.
- The arc names are clink:labelArc etc.
- The label and reference resources have names like clink:label etc.
- So the (extended) links contain only XLink arcs and XLink resources, some or many of them being ckey:conceptKey elements

For generic links:

- The name is typically cgen:link
- Arcs retain their name they have in the standard taxonomy
- Again, instead of locators there are taxonomy element keys (which are XLink resources)

Entrypoints will be discussed later, but note how we can easily leave out the "label linkbase" and "reference linkbase"
documents and still have a closed set of documents without any "dead keys".

So, if we want to do dimensional instance validation against a taxonomy in locator-free format, we can leave out
all "label linkbases" and "reference linkbases", and still have a closed taxonomy document set containing all
dimensional taxonomy data needed for the validation. In practice this means that more or less half of the taxonomy
does not have to be loaded into memory for dimensional instance validation (unless we need the labels, of course).

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

