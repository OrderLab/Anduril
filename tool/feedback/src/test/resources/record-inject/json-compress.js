'use strict'

const fs = require('fs')

const graph_json = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'))

const n = Number(graph_json.start)

graph_json.nodes = graph_json.nodes.filter(node => node.id < n)

const output = process.argv[3]

fs.writeFile(output, JSON.stringify(graph_json, null, 2), error => {
  if (error) {
    console.log(error)
  } else {
    console.log("JSON saved to", output)
  }
})
