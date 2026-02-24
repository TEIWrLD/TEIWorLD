# TEIWorLD (TEI Workflow for Language Data)
Tool to convert several written and spoken language data formats into TEI<br>
## Description
TEIWorLD transforms a variety of different formats for spoken and written language into the standardised formats TEISpoken and I5 with the intermediate format TEI P5. For archiving written data, the pipeline converts TEI P5 to the format used at IDS, the I5 format, which was developed by IDS based on TEI P5.<br>
<div align="center">
  <img src="images/Grafik_UML.png" alt="Schematic representation of the components of TEIWorLD">
</div>

## Usage
Command **spoken**:<br>
Converts to TEIspoken and keeps files separate if there is more than one in the input directory<br>
`de.ids.TeiWorld spoken path\to\input\dir\ path\to\output\dir\`

Command **written**:<br>
Converts to TEI I5 and combines files to a single corpus in case there is more than one in the input directory. The file `metadata.json` needs to be in the same directory<br>
`de.ids.TeiWorld written path\to\input\dir\ path\to\output\dir\`

Command **writtenP5**:<br>
Converts to TEI I5 and keeps files separate if there is more than one in the input directory<br>
`de.ids.TeiWorld writtenP5 path\to\input\dir\ path\to\output\dir\`

Command **writtenHierarchical**:<br>
Converts to TEI I5 and constructs the hierarchical document and text structure of a written corpus. 
The directory needs to contain the file `metadata.json` and one or more subdirectories (= idsDoc) that contain the individual texts (= idsText).<br>
`de.ids.TeiWorld writtenHierarchical path\to\input\dir\ path\to\output\dir\`

The folder structure of the input directory will be reflected in the XML tree:
```
CorpusStructured
├── Directory01                          // directory name = dokumentSigle 
│   ├── Kriterien für Datenaufnahme.txt  // file name = t.title
│   └── Protokoll Projekttreffen.docx    // file name = t.title
├── Directory02                          // directory name = dokumentSigle
│   └── Planung Publikation.docx         // file name = t.title
├── Directory03                          // directory name = dokumentSigle
│   ├── Briefsammlung.txt                // file name = t.title
│   ├── Essay.txt                        // file name = t.title
│   ├── Essay_Kommentare.pdf             // FILE WILL BE IGNORED as PDF is no valid input format
│   └── Workshop Korpusaufbau.docx       // file name = t.title
├── Directory04                          // DIRECTORY WILL BE IGNORED as there is no file as a direct child
│   └── folderXY                         // DIRECTORY WILL BE IGNORED, only files would be processed
│       ├── dummyFile02.txt              // FILE WILL BE IGNORED
│       └── emptyFolder                  // FILE WILL BE IGNORED
├── dummyFile01.txt                      // FILE WILL BE IGNORED as it is not inside a directory
└── metadata.json                        // MANDATORY file with the corpus metadata
```

<idsCorpus version="1.0">
    <idsHeader type="corpus" pattern="allesaußerZtg/Zschr" version="1.0"> <!-- contains metadata from meatadata.json -->
    <idsDoc type="text" version="1.0"> <!-- dokumentSigle: NOZ/Directory01 -->
      <idsHeader type="document" pattern="text" version="1.0">
      <idsText version="1.0">          <!-- textSigle: NOZ/Directory01.00001 | t.title: Kriterien für Datenaufnahme -->
	  <idsText version="1.0">          <!-- textSigle: NOZ/Directory01.00002 | t.title: Protokoll Projekttreffen -->
	</idsDoc>
	<idsDoc type="text" version="1.0"> <!-- dokumentSigle: NOZ/Directory02 -->
      <idsHeader type="text" pattern="text" version="1.0">
      <idsText version="1.0">          <!-- textSigle: NOZ/Directory02.00001 | t.title: Planung Publikation -->
	</idsDoc>
	<idsDoc type="text" version="1.0"> <!-- dokumentSigle: NOZ/Directory03 -->
      <idsHeader type="text" pattern="text" version="1.0">
	  <idsText version="1.0">          <!-- textSigle: NOZ/Directory03.00001 | t.title: Briefsammlung -->
	  <idsText version="1.0">          <!-- textSigle: NOZ/Directory03.00002 | t.title: Essay -->
	  <idsText version="1.0">          <!-- textSigle: NOZ/Directory03.00003 | t.title: Workshop Korpusaufbau -->
	</idsDoc>
</idsCorpus>


### Components
[TEIGarage](https://github.com/TEIC/TEIGarage)<br>
[TEICORPO](https://github.com/christopheparisse/teicorpo)<br>
P5ToI5<br>
### Data formats
#### Input (spoken formats)
eaf (Elan)<br>
textgrid (Praat)<br>
cha (chat/childes)<br>
trs (transcriber)<br>
maxqda (qdpx/mx24)<br>
#### Input (written formats)
txt<br>
docx/doc<br>
#### Output
ISO/TEI Transcriptions of Spoken Language (TEISpoken)<br>
IDS TEI P5 (I5)<br>

## Publications

## Team

## Contact
* [E-Mail](mailto:data-steward@ids-mannheim.de)

<hr style="border: 1px solid #ccc; margin-bottom: 20px;">
<footer>
  <div style="display: flex; justify-content: space-between; align-items: center;">
    <img src="images/IDS_Vorlage.svg" alt="Logo 1" style="width: 300px; height: 100px;">
    <img src="images/textplus_logo_RGB.png" alt="Logo 2" style="width: 100px; height: 100px;">
  </div>
  <p>&copy; 2025 TEIWorLD</p>
</footer>
