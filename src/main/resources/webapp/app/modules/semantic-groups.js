//
// modules/semantic-groups.js
//   Concept semantic groups constants.
//
/*global define */
define({
    groups: [
        {
            id: 'SPEC',
            name: 'Species',
            class_: 'species',
            regex: /:SPEC$/
        },
        {
            id: 'ANAT',
            name: 'Anatomy',
            class_: 'anatomy',
            regex: /:ANAT$/
        },
        {
            id: 'DISO',
            name: 'Disorders',
            class_: 'disorder',
            regex: /:DISO$/
        },
        {
            id: 'PATH',
            name: 'Pathways',
            class_: 'pathway',
            regex: /:PATH$/
        },
        {
            id: 'CHED',
            name: 'Chemicals',
            class_: 'chemical',
            regex: /:CHED$/
        },
        {
            id: 'ENZY',
            name: 'Enzymes',
            class_: 'enzyme',
            regex: /:ENZY$/
        },
        {
            id: 'MRNA',
            name: 'miRNA',
            class_: 'mrna',
            regex: /:MRNA$/
        },
        {
            id: 'PRGE',
            name: 'Genes and Proteins',
            class_: 'gene-protein',
            regex: /:PRGE$/
        },
        {
            id: 'COMP',
            name: 'Cellular Components',
            class_: 'component',
            regex: /:COMP$/
        },
        {
            id: 'FUNC',
            name: 'Molecular Functions',
            class_: 'function',
            regex: /:FUNC$/
        },
        {
            id: 'PROC',
            name: 'Biological Processes',
            class_: 'process',
            regex: /:PROC$/
        }
    ]
});
