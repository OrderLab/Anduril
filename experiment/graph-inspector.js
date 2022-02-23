'use strict'

const fs = require('fs')

const graph_json = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'))

const m = new Set(), b = new Set()

graph_json.injections.forEach(injection => {
	const t = injection.location.class + " - " + injection.location.method
	const i = injection.block
	if (!m.has(t)) {
		m.add(t)
	}
	if (!b.has(i)) {
		b.add(i)
	}
})

console.log('# of injections:', graph_json.injections.length)
console.log('# of unique basic blocks:', b.size,
	'(' + Number((b.size * 100.0 / graph_json.injections.length).toFixed(2)) + '%)')
console.log('# of unique function bodys:', m.size,
	'(' + Number((m.size * 100.0 / graph_json.injections.length).toFixed(2)) + '%)')
