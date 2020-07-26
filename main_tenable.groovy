def call() {

    sh 'rm -rf result.txt'

    println "****** uploading the csv ******"

    uploadFile.call()

    def output = ''

    input = readFile file: "input.csv", encoding: "ASCII"

    output = input.split('\n')

    output.each {

        def output1 = "${it}".split(',')

        create_tenable.call(output1)

        archive 'result.txt'

    }

}
