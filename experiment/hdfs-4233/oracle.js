'use strict'

const fs = require('fs')

const graph_json = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'))
const trials_dir = process.argv[3] + '/'

const targets = new Set()

graph_json.injections.forEach(injection => {
  if (injection.hasOwnProperty('location')) {
    const loc = injection['location']
    if (loc['class'] == 'org.apache.hadoop.hdfs.server.namenode.EditLogFileOutputStream' &&
        injection['invocation'] == 'void <init>(java.io.File,java.lang.String)' &&
        injection['exception'] == 'java.io.FileNotFoundException') {
      targets.add(injection.id)
    }
  }
})

console.log('target injections:', targets)

function check(o) {
  let symptom = false, ending = 0
  fs.readFileSync(o).toString().split('\n').forEach(line => {
    if (ending == 0 && line.includes('IPC Server handler') && line.includes(':Server$Handler@')) {
      ending = 1
    }
    if (ending == 1 && line.startsWith('java.io.IOException: Unable to start log segment')) {
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
        const dir_name = trials_dir + i + '/logs-1/'
        if (check(dir_name + fs.readdirSync(dir_name).filter(f => f.endsWith('.log'))[0])) {
          console.log(`exposure: ${i}`)
        }
      }
    }
  }
} catch(e) {
  console.log(`ends at ${i} with ${e}`)
}
