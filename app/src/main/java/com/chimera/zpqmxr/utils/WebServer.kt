package com.chimera.zpqmxr.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder

class WebServer(private val port: Int, private val rootHelper: (String) -> Unit) {
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val TAG = "WebServer"
    
    private val logQueue = mutableListOf<String>("[SYSTEM] Arsenal C2 Initialized. RNDIS Link Active.")

    fun start() {
        if (isRunning) return
        isRunning = true
        Thread {
            try {
                serverSocket = ServerSocket(port)
                Log.d(TAG, "Server started on port $port")
                while (isRunning) {
                    val client = serverSocket?.accept() ?: break
                    Thread { handleClient(client) }.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleClient(client: Socket) {
        try {
            client.use {
                val input = BufferedReader(InputStreamReader(it.getInputStream()))
                val output = PrintWriter(it.getOutputStream(), true)

                val requestLine = input.readLine() ?: return
                Log.d(TAG, "Request: $requestLine")
                
                
                while (true) {
                    val header = input.readLine()
                    if (header.isNullOrEmpty()) break
                }
                
                if (requestLine.startsWith("GET / ") || requestLine.startsWith("GET /index.html")) {
                    serveHtmlUi(output)
                } else if (requestLine.startsWith("GET /api/execute?script=") || requestLine.startsWith("GET /execute?script=")) {
                    val scriptPart = requestLine.substringAfter("script=").substringBefore(" HTTP/")
                    val script = URLDecoder.decode(scriptPart, "UTF-8")
                    
                    synchronized(logQueue) {
                        logQueue.add("> EXECUTING: ${script.lines().firstOrNull() ?: ""} ...")
                        if (logQueue.size > 100) logQueue.removeAt(0)
                    }
                    
                    rootHelper(script)
                    serveJsonResponse(output, """{"status":"ok", "message": "Payload delivered successfully."}""")
                } else if (requestLine.startsWith("GET /api/logs")) {
                    val logsJson = synchronized(logQueue) {
                        if (logQueue.isEmpty()) "[]"
                        else logQueue.joinToString(separator = "\",\"", prefix = "[\"", postfix = "\"]") { 
                            it.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "") 
                        }
                    }
                    serveJsonResponse(output, """{"status":"ok", "logs": $logsJson}""")
                } else if (requestLine.startsWith("GET /api/loot")) {
                    
                    
                    
                    val lootJson = "[]" 
                    serveJsonResponse(output, """{"status":"ok", "files": $lootJson}""")
                } else {
                    serveJsonResponse(output, """{"status":"error", "message": "Not Found"}""", 404)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun serveHtmlUi(output: PrintWriter) {
        val html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Arsenal C2 Interface</title>
                <style>
                    :root {
                        --bg-color: #0d1117;
                        --panel-bg: #161b22;
                        --border-color: #30363d;
                        --text-color: #c9d1d9;
                        --accent-blue: #58a6ff;
                        --accent-green: #3fb950;
                        --accent-red: #f85149;
                        --font-mono: 'JetBrains Mono', 'Courier New', Courier, monospace;
                    }
                    body {
                        margin: 0; padding: 0; background-color: var(--bg-color); color: var(--text-color);
                        font-family: var(--font-mono); display: flex; height: 100vh; overflow: hidden;
                    }
                    .sidebar {
                        width: 260px; background: var(--panel-bg); border-right: 1px solid var(--border-color);
                        display: flex; flex-direction: column; padding: 25px 20px; box-sizing: border-box;
                    }
                    .logo { font-size: 24px; font-weight: bold; color: var(--text-color); margin-bottom: 5px; letter-spacing: 1px; }
                    .status { font-size: 13px; color: var(--accent-green); margin-bottom: 40px; font-weight: bold; }
                    .status::before { content: "●"; margin-right: 6px; }
                    .nav-item { padding: 12px 15px; margin-bottom: 8px; cursor: pointer; border-radius: 6px; transition: 0.2s; font-size: 14px; background: transparent; color: #8b949e; border: 1px solid transparent; }
                    .nav-item:hover { background: rgba(139, 148, 158, 0.1); color: var(--text-color); }
                    .nav-item.active { background: rgba(88, 166, 255, 0.1); color: var(--accent-blue); border: 1px solid rgba(88, 166, 255, 0.4); }
                    
                    .main { flex: 1; display: flex; flex-direction: column; padding: 30px; box-sizing: border-box; overflow-y: auto; }
                    .section-title { font-size: 20px; border-bottom: 1px solid var(--border-color); padding-bottom: 10px; margin-bottom: 25px; color: var(--text-color); text-transform: uppercase; letter-spacing: 1px; }
                    
                    .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 20px; margin-bottom: 30px; }
                    .card { background: var(--panel-bg); border: 1px solid var(--border-color); border-radius: 6px; padding: 20px; cursor: pointer; transition: 0.2s; position: relative; }
                    .card:hover { border-color: #8b949e; transform: translateY(-2px); }
                    .card h4 { margin: 0 0 8px 0; color: var(--accent-blue); font-size: 16px; }
                    .card p { margin: 0; font-size: 13px; color: #8b949e; line-height: 1.5; }
                    
                    .terminal-container { flex: 1; display: flex; flex-direction: column; background: #010409; border: 1px solid var(--border-color); border-radius: 6px; position: relative; overflow: hidden; }
                    .terminal-header { background: var(--panel-bg); padding: 10px 15px; font-size: 12px; color: #8b949e; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between; }
                    .terminal { flex: 1; padding: 15px; font-size: 13px; color: var(--text-color); overflow-y: auto; white-space: pre-wrap; line-height: 1.5; }
                    
                    .loot-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
                    .loot-table th, .loot-table td { border-bottom: 1px solid var(--border-color); padding: 12px 15px; text-align: left; font-size: 14px; }
                    .loot-table th { color: #8b949e; font-weight: 600; text-transform: uppercase; font-size: 12px; letter-spacing: 1px; }
                    .loot-table tr:hover { background: rgba(139, 148, 158, 0.05); }
                    .btn-download { background: transparent; border: 1px solid var(--accent-blue); color: var(--accent-blue); border-radius: 6px; padding: 6px 12px; cursor: pointer; font-family: var(--font-mono); font-size: 12px; transition: 0.2s; }
                    .btn-download:hover { background: var(--accent-blue); color: #0d1117; }
                    
                    .custom-input { background: var(--panel-bg); border: 1px solid var(--border-color); color: var(--text-color); font-family: var(--font-mono); font-size: 14px; width: 100%; padding: 12px; border-radius: 6px; outline: none; transition: 0.2s; }
                    .custom-input:focus { border-color: var(--accent-blue); }
                    .btn-fire { background: var(--accent-red); color: #fff; border: none; border-radius: 6px; font-weight: bold; font-family: var(--font-mono); padding: 12px 30px; cursor: pointer; transition: 0.2s; }
                    .btn-fire:hover { opacity: 0.9; transform: translateY(-1px); }

                    .hide { display: none !important; }
                    
                    ::-webkit-scrollbar { width: 8px; }
                    ::-webkit-scrollbar-track { background: var(--bg-color); }
                    ::-webkit-scrollbar-thumb { background: #30363d; border-radius: 4px; }
                    ::-webkit-scrollbar-thumb:hover { background: #8b949e; }
                </style>
            </head>
            <body>
                <div class="sidebar">
                    <div class="logo">CHIMERA PRO</div>
                    <div class="status">RNDIS : ACTIVE</div>
                    <div class="nav-item active" onclick="switchTab('launcher', this)">> PAYLOAD LAUNCHER</div>
                    <div class="nav-item" onclick="switchTab('terminal', this)">> LIVE CONSOLE</div>
                    <div class="nav-item" onclick="switchTab('loot', this)">> LOOT EXPLORER</div>
                </div>
                
                <div class="main" id="view-launcher">
                    <div class="section-title">Payload Arsenal</div>
                    <div class="grid">
                        <div class="card" onclick="firePayload('DELAY 500\nGUI r\nDELAY 500\nSTRING cmd\nENTER\nDELAY 500\nSTRING color a && tree C:\\\nENTER')">
                            <h4>Windows Recon</h4>
                            <p>Opens CMD and runs a directory tree visualizer.</p>
                        </div>
                        <div class="card" onclick="firePayload('DELAY 500\nGUI r\nDELAY 500\nSTRING cmd\nENTER\nDELAY 500\nSTRING ipconfig /all\nENTER')">
                            <h4>Windows Network</h4>
                            <p>Opens CMD and dumps network configuration.</p>
                        </div>
                        <div class="card" onclick="firePayload('DELAY 500\nCTRL ALT t\nDELAY 500\nSTRING w\nENTER')">
                            <h4>Linux Sessions</h4>
                            <p>Opens terminal and runs w command.</p>
                        </div>
                    </div>
                </div>

                <div class="main hide" id="view-terminal">
                    <div class="section-title">Live Execution Console</div>
                    <div class="terminal-container">
                        <div class="terminal-header">
                            <span>SESSION: ttyUSB0</span>
                            <span id="term-status" style="color:var(--accent-green)">READY</span>
                        </div>
                        <div class="terminal" id="term-output"></div>
                    </div>
                    <div style="margin-top: 15px; display:flex; gap: 10px;">
                        <input type="text" id="custom-script" class="custom-input" style="flex:1;" placeholder="Enter raw DuckyScript here...">
                        <button class="btn-fire" onclick="fireCustom()">FIRE</button>
                    </div>
                </div>

                <div class="main hide" id="view-loot">
                    <div class="section-title">Exfiltrated Loot</div>
                    <table class="loot-table">
                        <thead>
                            <tr>
                                <th>Filename</th>
                                <th>Size</th>
                                <th>Date Extracted</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody id="loot-body">
                            <!-- Populated via API -->
                            <tr><td colspan="4" style="text-align:center; color:#555;">Loading...</td></tr>
                        </tbody>
                    </table>
                </div>

                <script>
                    function switchTab(viewId, el) {
                        document.querySelectorAll('.nav-item').forEach(e => e.classList.remove('active'));
                        el.classList.add('active');
                        
                        document.getElementById('view-launcher').classList.add('hide');
                        document.getElementById('view-terminal').classList.add('hide');
                        document.getElementById('view-loot').classList.add('hide');
                        
                        document.getElementById('view-' + viewId).classList.remove('hide');

                        if (viewId === 'loot') fetchLoot();
                    }

                    function writeTerm(msg) {
                        const t = document.getElementById('term-output');
                        t.innerHTML += msg + '\n';
                        t.scrollTop = t.scrollHeight;
                    }

                    function firePayload(script) {
                        
                        fetch('/api/execute?script=' + encodeURIComponent(script))
                            .then(r => r.json())
                            .then(data => writeTerm('[+] SUCCESS: ' + data.message))
                            .catch(err => writeTerm('[-] ERROR: ' + err));
                    }

                    function fireCustom() {
                        const script = document.getElementById('custom-script').value;
                        if (!script) return;
                        firePayload(script);
                        document.getElementById('custom-script').value = '';
                    }

                    let lastLogCount = 0;
                    function pollLogs() {
                        fetch('/api/logs')
                            .then(r => r.json())
                            .then(data => {
                                const t = document.getElementById('term-output');
                                if (data.logs && data.logs.length) {
                                    if (data.logs.length !== lastLogCount || lastLogCount === 0) {
                                        t.innerHTML = '';
                                        data.logs.forEach(l => t.innerHTML += l + '\n');
                                        t.scrollTop = t.scrollHeight;
                                        lastLogCount = data.logs.length;
                                    }
                                }
                                document.getElementById('term-status').innerText = 'LIVE';
                                document.getElementById('term-status').style.color = 'var(--accent-green)';
                            })
                            .catch(err => {
                                document.getElementById('term-status').innerText = 'DISCONNECTED';
                                document.getElementById('term-status').style.color = 'var(--accent-red)';
                            });
                    }

                    function fetchLoot() {
                        fetch('/api/loot')
                            .then(r => r.json())
                            .then(data => {
                                const b = document.getElementById('loot-body');
                                b.innerHTML = '';
                                if (data.files && data.files.length) {
                                    data.files.forEach(f => {
                                        b.innerHTML += `<tr>
                                            <td>${'$'}{f.name}</td>
                                            <td>${'$'}{f.size}</td>
                                            <td>${'$'}{f.date}</td>
                                            <td><button class="btn-download" onclick="alert('Downloading ${'$'}{f.name}... (Simulated)')">DOWNLOAD</button></td>
                                        </tr>`;
                                    });
                                } else {
                                    b.innerHTML = '<tr><td colspan="4" style="text-align:center; color:#555;">No loot found.</td></tr>';
                                }
                            });
                    }

                    setInterval(pollLogs, 2000);
                    pollLogs(); 
                </script>
            </body>
            </html>
        """.trimIndent()
        
        output.println("HTTP/1.1 200 OK")
        output.println("Content-Type: text/html")
        output.println("Content-Length: ${html.toByteArray().size}")
        output.println("Connection: close")
        output.println()
        output.println(html)
    }

    private fun serveJsonResponse(output: PrintWriter, json: String, code: Int = 200) {
        val statusText = if (code == 200) "OK" else "Not Found"
        output.println("HTTP/1.1 $code $statusText")
        output.println("Content-Type: application/json")
        output.println("Content-Length: ${json.toByteArray().size}")
        output.println("Connection: close")
        output.println()
        output.println(json)
    }
}
