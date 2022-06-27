'use strict'

const fs = require('fs')

const graph_json = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'))
const trials_dir = process.argv[3] + '/'

const targets = new Set()

graph_json.injections.forEach(injection => {
  if (injection.hasOwnProperty('location')) {
    const loc = injection['location']
    if (loc['class'] == 'org.apache.zookeeper.server.quorum.QuorumPeer' &&
        loc['method'] == 'long readLongFromFile(java.lang.String)' &&
        injection['invocation'] == 'java.lang.String readLine()' &&
        injection['exception'] == 'java.io.IOException') {
      targets.add(injection.id)
    }
  }
})

console.log('target injections:', targets)

function check(o) {
  let symptom = false
  fs.readFileSync(o).toString().split('\n').forEach(line => {
    if (line.startsWith('1) testPZxidUpdatedWhenLoadingSnapshot(org.apache.zookeeper.server.quorum.FuzzySnapshotRelatedTest)')) {
      symptom = true
    }
  })
  return symptom
}

let i = 0
try {
  for (i = 0; i < 1000000; i++) {
    const json = JSON.parse(fs.readFileSync(trials_dir + 'injection-' + i + '.json'))
    if (json.hasOwnProperty('id')) {
      if (targets.has(json.id)) {
        if (check(trials_dir + 'output-' + i + '.txt')) {
          console.log(`exposure: ${i}`)
        }
      }
    }
  }
} catch(e) {
  console.log(`ends at ${i} with ${e}`)
}
