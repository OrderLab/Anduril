'use strict'

const fs = require('fs')
const {execSync} = require('child_process')

const graph_json = JSON.parse(fs.readFileSync(__dirname + '/tree.json', 'utf8'))
const zk_path = __dirname + '/../../systems/zookeeper-3157/'
const run_script = __dirname + '/run-instrumented-test.sh'
const trial_dir = __dirname + '/trials/'
const injection_notice = 'FlakyAgent: injected the exception '

function run_trial(trialId, injectionId, times, exception, trace_file, output_file) {
	let output, result
	try {
		output = String(execSync(
			`bash ${run_script} ${zk_path} ${injectionId} ${times} ${exception} ${trace_file}`))
		result = 'good-run'
	} catch (ex) {
		output = String(ex.stdout)
		result = 'bad-run'
	}
	fs.writeFileSync(output_file, output)
	const injected =
		output.split('\n').some(line => line.startsWith('FlakyAgent: injected the exception ')) ?
		'injected' : 'not-injected'
	console.log(new Date().toISOString(), trialId, injectionId, times, exception, injected, result)
}

//run_trial(12, 59, -1, 'java.io.IOException', 'trace.txt', 'output.txt')
//run_trial(12, 59, 1, 'java.io.IOException', 'trace.txt', 'output.txt') // always bad run, but long
// run_trial(177, 800, 1, 'java.io.IOException', 'trace.txt', 'output.txt') // sometimes good run
// run_trial(180, 803, 1, 'java.io.IOException', 'trace.txt', 'output.txt') // sometimes good run
// run_trial(181, 804, 1, 'java.io.IOException', 'trace.txt', 'output.txt') // sometimes good run

let trialId = 0

for (let iter = 1; iter <= 3; iter++) {
	graph_json.injections.forEach(injection => {
		const output = `${trial_dir}${trialId}-${injection.id}-${iter}-${injection.exception}.out`
		const trace  = `${trial_dir}${trialId}-${injection.id}-${iter}-${injection.exception}.txt`
		const result = run_trial(trialId, injection.id, iter, injection.exception, trace, output)
		trialId++
	})
}
