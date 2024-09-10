# Tinkar Composer

Tinkar Composer provides a fluent interface for creating data for the Entity Model. This API is intended to accelerate developers creating Concepts, Semantics, and Patterns (and their associated Versions) by providing a declarative interface that abstracts many of the nuances of low-level data handling in favor of consistent writing and merging.

### Team Ownership - Product Owner
Architecture Team

## Composing

Composing new data requires four steps:

1. Instantiating a Composer:

    ```java
    Composer composer = new Composer("Composer Name");
    ```

2. Opening a Session from the Composer:
    ```java
    Session session = composer.open(status, time, author, module, path);
    ```

3. Composing a Concept, Semantic, or Pattern using the fluent API:
    * Concept Compose Example

      ```java
        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .attach((FullyQualifiedName fqn) -> fqn
                        .language(ENGLISH_LANGUAGE)
                        .text("FQN for Concept")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));
        Pattern Compose Example
        ```
    
    * Pattern Compose Example

      ```java
      session.compose((PatternAssembler patternAssembler) -> patternAssembler
        .meaning(patternMeaning)
        .purpose(patternPurpose)
        .fieldDefinition(fieldMeaning, fieldPurpose, fieldDataType)
        .attach((FullyQualifiedName fqn) -> fqn
                .language(ENGLISH_LANGUAGE)
                .text("FQN for Pattern")
                .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));
      ```
          
    * Semantic Compose Example - There are two ways to compose semantics; using a SemanticAssembler or a SemanticTemplate implementation.
       The examples below show equivalent data composed using each of the methods.

       1. Using a SemanticAssembler:

          ```java
          session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(referenceConcept)
                .pattern(DESCRIPTION_PATTERN)
                .fieldValues(fieldValues -> fieldValues
                        .with(ENGLISH_LANGUAGE)
                        .with("Synonym Text Here")
                        .with(DESCRIPTION_NOT_CASE_SENSITIVE)
                        .with(REGULAR_NAME_DESCRIPTION_TYPE))
                .attach((USDialect dialect) -> dialect
                        .acceptability(PREFERRED)));
            ```
      2. Using a SemanticTemplate implementation:
         
            ```java
            session.compose(new Synonym()
                .language(ENGLISH_LANGUAGE)
                .text("Synonym Text Here")
                .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE), referenceConcept)
                .attach(new USDialect()
                        .acceptability(ACCEPTABLE));
           ```

4. Closing the Session:

    ```java
    composer.closeSession(session);
    ```
 

## Sessions

### Opening Sessions

The Composer is responsible for opening and managing Sessions as a convenience for developers and to reduce redundant Object creation. A Session is associated 
with a single STAMP and any Components composed in the Session are written with this STAMP. To open a Session, the developer must supply the appropriate 
status, time (optional), author, module, and path to create the write STAMP. If time is not supplied, then it will be set as the time the Session is committed.

### Ending Sessions

Sessions will either be committed or cancelled. Committed sessions will commit written data to the datastore with the appropriate STAMP making it available 
for display, query, and search. Cancelled Sessions will set written data to a cancelled STAMP, which flags the data for deletion and removes it from 
display and search results.

The Composer can end (i.e., commit or cancel) a single session or all sessions managed by the Composer as shown below.

* Commit a Session: `composer.commitSession(session);`
* Commit all Sessions: `composer.commitAllSessions();`
* Cancel a Session: `composer.cancelSession(session);`
* Cancel all Sessions: `composer.cancelAllSessions();`

### Assemblers

Concepts, Semantics, and Patterns each have their own Assembler enabling a fluent interface to create each and attach additional Semantics referring to the assembled Component.

#### ConceptAssembler

The ConceptAssembler allows the developer to directly define the PublicId or provide a Concept Proxy containing one. As a convenience, it also allows the 
developer to supply a UUID to create or append to a PublicId. If not supplied (by any method described above) then a random PublicId will be assigned when the 
Concept is written.

#### PatternAssembler

The PatternAssembler allows the developer to provide a Pattern Proxy containing a PublicId. If not supplied then a random PublicId will be assigned when the 
Pattern is written. It also enables defining other Pattern attributes as well, such as meaning, purpose, and field definitions.

While meaning and purpose can be directly assigned, each field definition requires several properties including the meaning, purpose, and datatype for the 
field value it defines. For convenience, there are two methods to add a field definition, one allows the developer to specify the index of each field definition 
while the other will assign the indexes sequentially. The validation routine called prior to writing the Pattern will ensure the indexes have been properly 
assigned without overlapping or skipping indexes.

#### SemanticAssembler

The SemanticAssembler allows the developer to provide a Semantic Proxy containing a PublicId. If not supplied then a random PublicId will be assigned when the 
Semantic is written. It also enables defining other Semantic attributes as well, such as the reference, pattern, and field values.

The reference defines which Component the semantic information applies to. The pattern defines Pattern whose field definitions provide the meaning, purpose, 
and data type for the Semantic field values. The Semantic field value at each index is defined by and must match the data type associated with the defining Pattern 
field definition at the corresponding index.

### SemanticTemplates
SemanticTemplates provide a fluent interface and convenience methods to compose Semantics - each implementation will create a different Semantic type. The key 
benefit of SemanticTemplates is that they enable a more declarative interface, such that developers need not concern themselves with Semantic field value indexes 
and ordering, but instead are provided with declarative fluent methods as shown for the FullyQualifiedName SemanticTemplate below.

```java
session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
            .attach((FullyQualifiedName fqn) -> fqn
                    .language(ENGLISH_LANGUAGE)
                    .text("Fully Qualified Name Text Here")
                    .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));
```

#### Predefined Templates

The most common Semantics have been predefined as SemanticTemplates to promote consistency and ease-of-use. These include Semantics such as 
FullyQualifiedName, Synonym, Definition, Identifiers, some dialect and membership semantics, etc. Using predefined SemanticTemplates to compose a Semantic 
and/or attach it to another Component is as easy as supplying an instance as a Session compose method parameter or providing the corresponding Consumer to one 
of the Attachable methods as shown in the equivalent examples below.

Session compose method example:

```java
session.compose(new FullyQualifiedName()
                .language(ENGLISH_LANGUAGE)
                .text("Fully Qualified Name Text Here")
                .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE), referencedComponent);
```

Attachable method example:

```java
session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
            .concept(referencedComponent)
            .attach((FullyQualifiedName fqn) -> fqn
                    .language(ENGLISH_LANGUAGE)
                    .text("Fully Qualified Name Text Here")
                    .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));
```

#### Custom Templates

For use cases that require other Semantic types, the SemanticTemplate interface can be extended such that developers can create new SemanticTemplate 
implementations and use them within the Tinkar Composer API. As with all interfaces, custom templates must define all unimplemented methods to properly 
extend the SemanticTemplate class. At this time, custom SemanticTemplates must be composed and/or attached as a method parameter (as shown below) since 
they do not have a corresponding Consumer configured in the Attachable interface.
Session compose method example:

```java
session.compose(new CustomSemantic()
                        .text("Custom Semantic Example"), referencedComponent);
```

Attachable method example:

```java
session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
            .concept(referencedComponent)
            .attach(new CustomSemantic()
                        .text("Custom Semantic Example")));
```

## Issues and Contributions
Technical and non-technical issues can be reported to the [Issue Tracker](https://github.com/ikmdev/tinkar-composer/issues).

Contributions can be submitted via pull requests. Please check the [contribution guide](doc/how-to-contribute.md) for more details.

