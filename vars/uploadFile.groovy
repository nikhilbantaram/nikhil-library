def call() {

    _fileUpload()

}

def _fileUpload() {

    def inputFile = input message: 'Upload file', parameters: [file(name: 'input.csv')]

    writeFile(file: 'input.csv', text: inputFile.readToString())

    stash name: 'data', includes: 'input.csv'
    
}