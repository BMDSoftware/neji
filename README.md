![Neji](https://github.com/BMDSoftware/neji/raw/master/wiki/logo.png)

Neji is a flexible and powerful platform for biomedical information extraction from scientific texts, such as patents, publications and electronic health records.


## Table of contents
- [What is new in Neji 2?](#what-is-new-in-neji-2?)
- [What you can do with Neji?](#what-you-can-do-with-neji?)
- [Quick start](#quick-start)
- [Documentation](#documentation)
- [Usage notification](#usage-notification)
- [Support and consulting](#support-and-consulting)
- [Bugs and features requests](#bugs-and-features-requests)
- [Creators and contributors](#creators-and-contributors)
- [Copyright and license](#copyright-and-license)

## What is new in Neji 2?
- **Neji Web Server** 
	- Management of annotation services and respective dictionaries and machine-learning models
	- Web page with interactive annotation for each service
	- REST API for each service
- **Gimli for machine learning NER training**
	- Gimli is now easier to use with faster training and processing times. Its functionalities are now integrated into Neji, providing the same high accuracy previously achieved
- Multiple linguistic parsers support, for general text and multi-language
- Support to additional input and output formats, including BioC
- SDK usability improvements
- Performance improvements
- Stability improvements


## What you can do with Neji?
With Neji you can build text mining processing pipelines for:

- Rapidly **create REST services** and interactive **web pages** for text mining tasks
- **Concept recognition:**
    - Dictionary-based, Machine learning-based and Rule-based
- **Train machine learning models for NER (Named Entity Recognition):**
	- Normalization with dictionary matching and Stopword filtering
- **Linguistic parsing:**
    - Sentence splitting, Tokenisation, Lemmatisation, Chunking and Dependency parsing
- **Convert between corpora formats:**
	- **Input formats:** BioC, XML, HTML and Text 
	- **Output formats:** JSON, A1, BC2, Base64, BioC, CoNLL, IeXML, Pipe and PipeExtended


## Quick start
1. Download and extract the [latest version of Neji](https://github.com/BMDSoftware/neji/releases/download/v2.0.0/neji-2.0.0.zip)
2. Use `neji.sh` to annotate
3. Use `nejiTrain.sh` to train new NER models

## Documentation
Neji's documentation is available at [https://github.com/BMDSoftware/neji/wiki](https://github.com/BMDSoftware/neji/wiki).

## Usage notification
If you are using Neji in your projects, please let us know by sending an e-mail to [david.campos@bmd-software.com](mailto:david.campos@bmd-software.com).

## Bugs and features requests
Have a bug or a feature request?

If your problem or idea is not addressed yet, please [open a new issue](https://github.com/BMDSoftware/neji/issues/new).


## Support and consulting
[<img src="https://github.com/BMDSoftware/neji/raw/master/wiki/bmd.png" height="64">](https://www.bmd-software.com)

Please contact [BMD Software](https://www.bmd-software.com) for professional support and consulting services.


## Copyright and license
Copyright (C) 2016 BMD Software and University of Aveiro

Neji is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. To view a copy of this license, visit [http://creativecommons.org/licenses/by-nc-sa/3.0/](http://creativecommons.org/licenses/by-nc-sa/3.0/).
