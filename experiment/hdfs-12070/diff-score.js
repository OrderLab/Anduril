'use strict'

const fs = require('fs')

function getEntries(f) {
	const entries = []
	fs.readFileSync(f).toString().split('\n').forEach(line => {
		if (line.match(/.*\ \-\ [A-Z]+\ *\[.*\]\ \-\ /) != null) {
			const s = line.match(/\-\ [A-Z]+\ *\[.*\]\ \-\ /)[0]
			const a = s.indexOf('[')
			const b = s.lastIndexOf(':')
			const c = s.lastIndexOf('@')
			const d = s.lastIndexOf(']')
			entries.push({
				th : s.substring(a + 1, b),
				f  : s.substring(b + 1, c),
				nu : s.substring(c + 1, d),
				msg: line.substring(line.indexOf(s) + b + 1, line.length),
			})
		}
	})
	return entries
}

const e1 = getEntries(process.argv[2]), e2 = getEntries(process.argv[3])
const diff_file = process.argv[4], bad_run_log = process.argv[3]
const spec_json = JSON.parse(fs.readFileSync(process.argv[5], 'utf8'))

const ground_truth_diff = new Set()
fs.readFileSync(diff_file).toString().split('\n').forEach(line => {
	if (line.length > 0) {
		ground_truth_diff.add(line)
	}
})

function getMap(entries) {
	const m = new Map()
	entries.forEach(entry => {
		if (!m.has(entry.th)) {
			m.set(entry.th, [])
		}
		m.get(entry.th).push({
			f  : entry.f,
			nu : entry.nu,
			msg: entry.msg,
		})
	})
	return m
}

const m1 = getMap(e1), m2 = getMap(e2)
const d1 = [], d2 = [], common = []

m1.forEach((entries, th) => {
	if (!m2.has(th)) {
		d1.push(th)
	} else {
		common.push(th)
	}
})
m2.forEach((entries, th) => {
	if (!m1.has(th)) {
		d2.push(th)
	}
})

common.sort()

function compute(a, b) {
	const dp = [[]]
	const opt = [[]]
	for (let i = 0; i <= b.length; i++) {
		dp[0].push(0)
		opt[0].push(0)
	}
	for (let i = 0; i < a.length; i++) {
		const p = dp[dp.length - 1]
		const nx = []
		dp.push(nx)
		const no = []
		opt.push(no)
		for (let j = 0; j <= b.length; j++) {
			nx.push(p[j])
			no.push(1)
		}
		for (let j = 0; j < b.length; j++) {
			if (a[i].f == b[j].f && a[i].nu == b[j].nu) {
				if (nx[j + 1] < p[j] + 1) {
					nx[j + 1] = p[j] + 1
					no[j + 1] = 2
				}
			}
		}
		for (let j = 0; j < b.length; j++) {
			if (nx[j + 1] < nx[j]) {
				nx[j + 1] = nx[j]
				no[j + 1] = 0
			}
		}
	}
	return {
		len : dp[a.length][b.length],
		path: opt,
	}
}

// common.forEach(th => {
// 	const len = compute(m1.get(th), m2.get(th))
// 	console.log(th + ',' + m1.get(th).length + ',' + m2.get(th).length + ',' + len)
// })

function pp(th) {
	const a1 = m1.get(th)
	const a2 = m2.get(th)
	const res = compute(a1, a2)
	const cp = []
	const bad_log = []
	const c = '\\'
	let i = a1.length, j = a2.length
	while (i != 0 || j != 0) {
		if (res.path[i][j] == 0) {
			j--
			cp.push(c + a2[j].msg)
			bad_log.push({
				f  : a2[j].f,
				nu : a2[j].nu,
			})
		} else if (res.path[i][j] == 1) {
			i--
			cp.push(a1[i].msg + c)
		} else {
			i--
			j--
			cp.push(a1[i].msg + c + a2[j].msg)
		}
	}
	cp.reverse()
	// for (i = 0; i < cp.length; i++) {
	// 	console.log(cp[i])
	// }
	// m1.get(th).forEach(e => {
	// 	console.log(e.f, e.nu)
	// })
	// console.log('-'.repeat(80))
	// m2.get(th).forEach(e => {
	// 	console.log(e.f, e.nu)
	// })
	return bad_log
}

// console.log(m1.get('LearnerCnxAcceptorHandler-/127.0.0.1:2889'))

// console.log(m2.get('LearnerCnxAcceptorHandler-/127.0.0.1:2889'))

// console.log(common)
m2.forEach((value, key) => {
	if (!common.includes(key)) {
		value.forEach(entry => {
			// console.log(entry.f, entry.nu)
			const v = entry.f + ' ' + entry.nu
			if (ground_truth_diff.has(v)) {
				ground_truth_diff.delete(v)
			}
		})
	}
})
common.forEach(th => {
	// console.log(th, pp(th).length)
	pp(th).forEach(entry => {
		// console.log(entry.f, entry.nu)
		const v = entry.f + ' ' + entry.nu
		if (ground_truth_diff.has(v)) {
			ground_truth_diff.delete(v)
		}
	})
})


const startNumber = spec_json.start
const result = []

ground_truth_diff.forEach(v => {
	const arr = v.split(' ')
	const cls = '.' + arr[0], line = Number(arr[1])
	spec_json.nodes.forEach(node => {
		if (node.id < startNumber) {
			if (node.type == 'location_event') {
				if (node.location.class.endsWith(cls) && node.location.line_number == line) {
					result.push(node.id)
				}
			}
		}
	})
})

var symptom = false, ending = 0
fs.readFileSync(bad_run_log).toString().split('\n').forEach(line => {
  if (line.startsWith('Time: ') && ending == 0) {
    ending = 1
  }
	if (ending == 1 && line.startsWith('1) testBlockRecoveryRetryAfterFailedRecovery(org.apache.hadoop.hdfs.TestLeaseRecovery)')) {
	  ending = 2
  }
	if (ending == 2 && line.startsWith('java.lang.AssertionError: File should be closed')) {
    ending = 3
  }
	if (ending == 3 && line.includes('at org.apache.hadoop.hdfs.TestLeaseRecovery.testBlockRecoveryRetryAfterFailedRecovery(TestLeaseRecovery.java:272)')) {
	  symptom = true
  }
})
if (!symptom) {
  result.push(0)
}

console.log(result)



// function qq(arr) {
// 	arr.forEach(a => {
// 		pp(a)
// 		console.log('='.repeat(80))
// 	})
// }

// const hh = 'LearnerCnxAcceptorHandler-/127.0.0.1:2889'

// const a = [
// 	'ListenerHandler-/127.0.0.1:3887',
// 	'ListenerHandler-/127.0.0.1:3888',
// 	'ListenerHandler-/127.0.0.1:3889',]
// const b = [
// 	'RecvWorker:1',
// 	'RecvWorker:2',
// 	'RecvWorker:3',]
// const c = [
// 	'SendWorker:1',
// 	'SendWorker:2',
// 	'SendWorker:3',]

// qq(c)
