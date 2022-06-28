'use strict'

const fs = require('fs')

const graph_json = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'))
const trials_dir = process.argv[3] + '/'

const targets = new Set()

graph_json.injections.forEach(injection => {
  if (injection.hasOwnProperty('location')) {
    const loc = injection['location']
    if (loc['class'] == 'org.apache.hadoop.hdfs.server.namenode.TransferFsImage' &&
        loc['method'] == 'org.apache.hadoop.hdfs.server.namenode.TransferFsImage$TransferResult uploadImageFromStorage(java.net.URL,org.apache.hadoop.conf.Configuration,org.apache.hadoop.hdfs.server.namenode.NNStorage,org.apache.hadoop.hdfs.server.namenode.NNStorage$NameNodeFile,long,org.apache.hadoop.hdfs.util.Canceler)' &&
        injection['invocation'] == 'void <init>(java.net.URL,java.lang.String)' &&
        injection['exception'] == 'java.net.MalformedURLException') {
      targets.add(injection.id)
    }
  }
})

console.log('target injections:', targets)

function check(o) {
  var symptom = false, ending = 0
  fs.readFileSync(o).toString().split('\n').forEach(line => {
    if (line.startsWith('Time: ') && ending == 0) {
      ending = 1
    }
    if (ending == 1 && line.startsWith('1) testRollBackImage(org.apache.hadoop.hdfs.TestRollingUpgrade)')) {
      ending = 2
    }
    if (ending == 2 && line.startsWith('java.lang.AssertionError: Query return false')) {
      ending = 3
    }
    if (ending == 3 && line.includes('at org.apache.hadoop.hdfs.TestRollingUpgrade.queryForPreparation(TestRollingUpgrade.java:155)')) {
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
