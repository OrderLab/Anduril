'use strict'

const fs = require('fs')

const graph_json = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'))
const trials_dir = process.argv[3] + '/'

// const targets = new Set()
// 
// graph_json.injections.forEach(injection => {
//   if (injection.hasOwnProperty('location')) {
//     const loc = injection['location']
//     if (loc['class'] == 'org.apache.zookeeper.server.persistence.FileTxnLog' &&
//         injection['invocation'] == 'void force(boolean)' &&
//         injection['exception'] == 'java.io.IOException') {
//       targets.add(injection.id)
//     }
//   }
// })
// 
// console.log('target injections:', targets)

function check(o) {
  let symptom = false
  fs.readFileSync(o).toString().split('\n').forEach(line => {
    if (line.includes('Severe unrecoverable error, from thread : SyncThread:')) {
      symptom = true
    }
  })
  return symptom
}

let i = 0
try {
  for (i = 0; i < 1000000; i++) {
    if (check(trials_dir + 'output-' + i + '.txt')) {
      console.log(`exposure: ${i}`)
    }
  }
} catch(e) {
  console.log(`ends at ${i} with ${e}`)
}
