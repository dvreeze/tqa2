=======================
Locator-free taxonomies
=======================

Why do we need a locator-free model?
====================================

XBRL is a rich set of standards, especially regarding XBRL taxonomies (the metadata describing
XBRL instance data). This is especially true if taxonomies contain formula and table linkbases.
The XML representation of XBRL taxonomies is quite verbose, however. Still, that is not the main problem
with XBRL taxonomies (in XML format).

The real issue is that the documents making up a taxonomy are hard to decouple from each other. The URL references in XLink locators,
XLink simple links and xs:import schemaLocation attributes lead to many (often circular) dependencies across taxonomy
documents. One of the reasons for this "dependency mess" is that taxonomy documents play 2 different roles: carry semantics, and contribute
to DTS (discoverable taxonomy set) discovery. Another reason is that in order to interpret even one XLink arc we may have to
consult multiple XML documents (if XLink locators are used). Hence, to put it differently, standard XBRL taxonomies in XML format
*do not promote local reasoning* about their content.

This tight coupling among taxonomy documents has several consequences:

- To reduce memory footprint (and speed up loading) it would be nice to load a subset of the taxonomy documents where desired, but that's impossible without breaking closure under DTS discovery rules
- An in-memory model based on the document DOM trees would have the same tight coupling, again making it hard to reduce memory footprint while retaining closure under DTS discovery rules
- Adding an extension taxonomy to a base taxonomy would be more complex in memory than just "adding 2 taxonomies"
- Programmatically creating taxonomies (like test taxonomies for XBRL processing code) is harder than needed due to the complex "tangling" such software must do
- The same is true for editing (small) taxonomies by hand, which is now much too difficult
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

Note that in order to convert a locator-free taxonomy back to a standard taxonomy the URL dependencies have to be inserted
again, but at least that is a local challenge (possibly supported by pluggable heuristics) instead of an issue that plays up
all of the time.

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
- This reminds somewhat of table and formula linkbases in standard taxonomies, which also lean heavily on XLink resources and much less on XLink locators
- Schema files in the locator-free model never contain any xs:include elements, and therefore they do not support chameleon schemas
- Schema files in the locator-free model contain no schemaLocation attributes on xs:import elements
- That is, unless they act as "entrypoints" in the locator-free model, but more about that later
- Other than "entrypoints", all taxonomy files are "standalone" in that they contribute nothing to a DTS other than themselves
- After all, locator-free linkbases and "standalone" schemas contain no URL references (not even xbrldt:typedDomainRef attributes)

So locators have been replaced by (mostly) semantic keys, and combining documents into a set of taxonomy documents is
a bit like "database joins". The semantic keys do not care about the location of the taxonomy element referred to.

So, what does that mean for the XML elements used in the locator-free model?

For "standalone" schemas:

- They are pretty much the same as the standard XBRL counterpart
- The schemaLocation attribute in xs:import elements disappears
- The xbrldt:typedDomainRef attribute (in xs:element) becomes cxbrldt:typedDomainKey, which is typed xs:QName

See for example `venj-bw2-axes.xsd`_.

For linkbases:

- The outer element becomes clink:linkbase (so another namespace, so it is not an XBRL linkbase)

For presentation, definition and calculation extended links:

- The names are clink:presentationLink etc. (so another namespace, so it is not an XBRL standard link)
- The arc names are clink:presentationArc etc. (ater all, not a standard arc according to XBRL)
- The locators have been replaced by ckey:conceptKey elements, which are XLink resources
- So the (extended) links contain only XLink arcs and concept keys, which are XLink resources
- There may be clink:roleRef and clink:arcroleRef elements, which are not XLink simple links, and contain no href attribute
- Arcroles, link roles and resource roles are the same as in the standard taxonomy

See for example `venj-bw2-decree-on-additional-regulations-for-the-management-report-pre.xml`_.

For (standard) label and reference extended links:

- The names are clink:labelLink etc.
- The arc names are clink:labelArc etc.
- The label and reference resources have names like clink:label etc.
- So the (extended) links contain only XLink arcs and XLink resources, some or many of them being ckey:conceptKey elements

See for example `venj-bw2-axes-ref.xml`_.

For generic links:

- The name is typically cgen:link
- Arcs retain their name they have in the standard taxonomy
- Again, instead of locators there are taxonomy element keys (which are XLink resources)
- Non-key XLink resources retain their names they have in the standard taxonomy

See for example `venj-bw2-generic-linkrole-order.xml`_.

Entrypoints will be discussed later, but note how we can easily leave out the "label linkbase" and "reference linkbase"
documents and still have a closed set of documents without any "dead keys".

So, if we want to do dimensional instance validation against a taxonomy in locator-free format, we can leave out
all "label linkbases" and "reference linkbases", and still have a closed taxonomy document set containing all
dimensional taxonomy data needed for the validation. In practice this means that more or less half of the taxonomy
does not have to be loaded into memory for dimensional instance validation (unless we need the labels, of course).

.. _`venj-bw2-axes.xsd`: https://github.com/dvreeze/tqa2/blob/master/jvm/src/test/resources/testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes.xsd
.. _`venj-bw2-decree-on-additional-regulations-for-the-management-report-pre.xml`: https://github.com/dvreeze/tqa2/blob/master/jvm/src/test/resources/testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/presentation/venj-bw2-decree-on-additional-regulations-for-the-management-report-pre.xml
.. _`venj-bw2-axes-ref.xml`: https://github.com/dvreeze/tqa2/blob/master/jvm/src/test/resources/testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes-ref.xml
.. _`venj-bw2-generic-linkrole-order.xml`: https://github.com/dvreeze/tqa2/blob/master/jvm/src/test/resources/testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/presentation/venj-bw2-generic-linkrole-order.xml

Networks of relationships
=========================

One reason that a locator-free taxonomy can be used instead of its standard XBRL counterpart for instance validation
scenarios is that it retains the semantics expressed in the standard XBRL taxonomy.

This holds also for prohibition and overriding of relationships, and networks of relationships. Let's describe why
this intuitively holds.

First of all, arcs in both models are the same, except that the arc name may be in another namespace (but these mappings
map uniquely to a name in both directions). In particular, if an arc in the standard taxonomy represents 4 relationships, then so
does its counterpart in the corresponding locator-free taxonomy, and vice versa (because xlink:from, xlink:to and xlink:label
attributes are the same in both models). Moreover, attributes like the use and prohibit attribute can be used in both models.

*Base sets of arcs* are grouped based on the combination of:

- Arc expanded name (that is, local name plus namespace)
- Arcrole of the arc (xlink:arcrole attribute, that is)
- The parent element's expanded name (note that the parent is an extended link)
- The parent element's role (xlink:role attribute, that is)

Not surprisingly, this holds as much for the locator-free model as it does for standard taxonomies. The correspondence
of base sets between the 2 models is completely trivial, mapping only arc names and extended link names (in practice this
means mapping the namespace).

Equivalence of relationships works pretty much the same in both models, with rather obvious differences to account
for the difference between XLink locators and (locator-free) taxonomy element keys. Therefore, computing a
network of relationships from a base set of relationships (by prohibition/overriding resolution) is quite similar
in both models. Hence it should not come as a surprise that networks of relationships in both models trivially correspond to each other.

Given that both taxonomy representations are so closely related, and that the tight coupling in standard taxonomies often
gets in the way, would it be an idea to use locator-free taxonomies during taxonomy development and only convert to
standard taxonomies when publishing them?

Creating locator-free taxonomies programmatically
=================================================

Creating locator-free taxonomies programmatically is a lot easier than doing so for standard taxonomies.

For example, let's create a presentation arc, for a presentation relationship between 2 concepts. In the locator-free
case, it is like this:

- Create a concept key for the source concept (easy, because we know the concept name, but mind the namespace prefix)
- The same for the target concept
- Then create the arc connecting the two

Not so in the case of standard taxonomies, where instead of creating a concept key we need to look up the URL with (XPointer)
fragment to the concept declaration in some schema file, and then create the XLink locator to point to that. Granted,
the XPointer is mostly an ID, provided that the concept declaration does have an ID attribute.

Yet choosing a namespace prefix for a namespace (for concept keys, for example) when programmatically creating
locator-free taxonomies can be cumbersome. This is not XBRL-specific, of course.

So how do we programmatically create arbitrary XML with namespaces without too much effort? That's what yaidom2
offers, with its "node builder" element implementation. It has an element creation API that keeps a namespace prefix
administration under the hood, thus removing much of the pain of creating XML in a program.

There is much more to say about this, but that can be checked in the code that converts standard taxonomies to
their locator-free counterparts. For example:

- Yaidom2 DocumentENameExtractor instances are used to find out about used namespaces
- This is used in turn to clean up created XML by removing unused namespaces
- The element creation API avoids the default namespace, and helps in avoiding prefix-namespace "conflicts" (that is, the same prefix being used for more than 1 namespace in a document)
- This makes it easier to reason about correctness of the created XML (with namespaces potentially being used in attribute values and element text)

Entrypoints
===========

Not much has been said about (locator-free) entrypoints above. Most locator-free taxonomy documents are "standalone",
in that they do not and cannot contribute anything else to a DTS than themselves. In other words, they contain no
URL references to any other document. All locator-free linkbases fall into this category. So do all schema files, unless
they have at least one xs:import element with a schemaLocation attribute or at least one clink:linkbaseRef element.

Multiple entrypoints should be able to refer to (much of) the same "standalone" taxonomy documents, just like
multiple entrypoints in standard taxonomies can (directly or indirectly) refer to pretty much the same sets of documents.
That is indeed the case in the locator-free model.

Single-document entrypoints in the locator-free model directly sum up the complete DTS, unlike their standard taxonomy
entrypoint counterparts. These locator-free entrypoints contain xs:import elements with schemaLocation attribute and/or
clink:linkbaseRef elements (with href attribute).

TODO Add example entrypoint file.

To prevent the tangling of standard taxonomy documents, only 1 level of URL indirection is allowed. That is, a schema
document acting as entrypoint may refer to many other documents, but these referred documents must all be standalone
taxonomy documents. What that means for extension taxonomies is discussed in the next section.

With entrypoints summing up entire DTSes (without there being any DTS "discovery"), it is very easy to filter DTSes
by filtering the imports and linkbaseRefs in the entrypoint. Earlier it was mentioned that labels and references may
be uninteresting when using a taxonomy for (dimensional) instance validation. That is easily supported in the locator-free
model: just remove the corresponding linbaseRefs. It is easy to write software to do that for us.

By the way, an entrypoint in the locator-free model may be multiple documents taken together, but the constraint mentioned above must still hold.

Extension taxonomies
====================

How does this "at most one level of URL indirection" constraint support extension taxonomies? Suppose in the locator-free
model we have entrypoint file A for a DTS in a published taxonomy, and we have ad-hoc extension taxonomy entrypoint file B,
which would like to import A.

The latter import is still possible, but without the use of a schemaLocation attribute, or else we would violate
the "one level of URL indirection" constraint. Locator-free taxonomy validation software would check that the xs:import
(without schemaLocation) is honored when reasoning about the combined taxonomy, so effectively we still have entrypoint file
B "pulling in" entrypoint file A.

This makes sense if we think of entrypoint A as the target namespace of that schema document, instead of the URL. It's still
the same document, but referred to in a different way. Schema document B does not need to refer to the URL of schema A, as
long as the namespace in the xs:import is the target namespace of "some" schema, and that schema happens to be schema A.
The entrypoint would then be the set of documents B and A, and the "one level of URL indirection" requirement would be met.

The locator-free model does have its constraints, to make this all work. For example, it is expected that all schemas
have a targetNamespace, and that xs:include does not occur, and that all schemas have a unique targetNamespace. More on that follows below.

Typical scenarios for entrypoints in the locator-free model are:

- Single document entrypoints, that are themselves not standalone (obviously)
- Extension taxonomies using 2 entrypoints as described above, both of them not being standalone (obviously)
- Ad-hoc multi-document entrypoints, with one entrypoint not being standalone, but the other ones being standalone (for example formula linkbase files)
- Sets of standalone taxonomy documents for one ELR (if taxonomies are modelled around ELRs), where the ad-hoc entrypoint is the entire document set

So combined with the filtering of entrypoints mentioned earlier, there is much flexibility in how one can organize
entrypoints using the same set of standalone taxonomy documents.

Which taxonomies can be represented?
====================================

As mentioned earlier, not all XBRL-valid taxonomies can be represented in the locator-free model. For example, the use of
xs:include is not allowed, and therefore chameleon schemas are not allowed. This should hardly be a restriction in practice.
After all, suppose that in a standard taxonomy an XLink locator refers to a global element declaration in a chameleon schema,
what would that even mean? It could refer to one concept, which is only known after "resolving" the xs:include (so that's even less
local reasoning than is normally the case when interpreting arcs with locators). Even worse, it could point to a collection of
concepts (in different namespaces)! No wonder xs:include should not be used in taxonomies (neither in standard taxonomies nor
in their locator-free counterparts, if there were any).

It is not a far stretch to go from the requirement of not using xs:include to the requirement that all schemas must have a
target namespace. That would be helpful, because namespaces play an important role in the locator-free model, for example in
concept keys.

This leads to the following namespace-related requirements on locator-free taxonomy schemas and on the corresponding standard
taxonomy schemas for which a locator-free counterpart exists:

- No use of xs:include
- All taxonomy schemas have a target namespace
- All taxonomy schemas have a unique target namespace, so there is a one-to-one correspondence between taxonomy schema documents and target namespaces

Most XBRL taxonomies seem to obey these requirements. In case they don't, a pre-processing step may help to "enforce" these
requirements. For example, a "pure entrypoint schema" without target namespace can be forced to get a target namespace (in-memory)
without changing the semantics of the taxonomy. The same holds for schemas defining only role types and arcrole types.

Another requirement has to do with taxonomy element keys that use URLs (that is, an XLink locator rewritten as XLink resource
containing the same absolute href). Note that there are several "semantic" taxonomy element keys, to point to concept declarations,
other global element declarations, named type definitions, role type definitions, etc. The "any element keys" (containing a URL) are
the fallback mechanism for pointing to XML elements for which no more descriptive key exists. The requirement would then be
that the fragment part in the href URL is just an ID (shorthand XPointer), instead of a more complex XPointer. After all, IDs
are stable when converting a standard taxonomy to its locator-free counterpart, but that is not the case for other XPointer references.

So this leads to the following extra requirement on standard taxonomies (in order to be convertible to locator-free taxonomies):

- Each XML element referred to by some XLink locator must have an ID attribute, unless it is a global element declaration, named type definition or (arc)role type

If thie requirement is not met by the standard taxonomy, a pre-processing step is needed to generate IDs.

There are also requirements on locator-free taxonomies that are more XML related, and that imply similar requirements on
standard taxonomies that can be converted to the locator-free model. This could be constraints like not using a default namespace
and consistent use of namespace prefixes across taxonomy documents. Fortunately, many of these requirements can be met
after a pre-processing step, in which for example schemas are converted to do away with the default namespace or the "xsd"
prefix (instead of "xs").

For ease of conversion, it is also required that embedded linkbases are not used, and that the document root elements are xs:schema
or link:linkbase in the standard taxonomy.

Conclusion
==========

The locator-free taxonomy model has the same modelling power and semantics as standard taxonomies (taking a few restrictions
into account), but in a far more loosely coupled way. This opens up some interesting possibilities, none of them seeming to be
spectacular, but still potentially making quite a difference when used on a large scale.
