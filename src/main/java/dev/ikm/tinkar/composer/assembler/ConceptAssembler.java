package dev.ikm.tinkar.composer.assembler;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.composer.Attachable;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Concept;

import java.util.UUID;

public class ConceptAssembler extends Attachable {

    private Concept concept;
    private PublicId publicId;

    public ConceptAssembler concept(Concept concept) {
        this.concept = concept;
        return this;
    }

    public Concept concept() {
        if (concept == null) {
            concept = Concept.make(publicId());
        }
        return concept;
    }

    public ConceptAssembler publicId(PublicId publicId) {
        this.publicId = publicId;
        return this;
    }

    public PublicId publicId() {
        if (publicId == null) {
            publicId = PublicIds.newRandom();
        }
        return publicId;
    }

    public ConceptAssembler uuid(UUID uuid) {
        UUID[] uuids = new UUID[publicId.uuidCount() + 1];
        uuids[publicId.uuidCount()] = uuid;
        publicId = PublicIds.of(uuids);
        return this;
    }

    @Override
    EntityProxy asReference() {
        return concept();
    }
}
