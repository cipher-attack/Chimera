package com.chimera.zpqmxr.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.yield
import kotlin.random.Random


enum class InterpreterState {
    IDLE, PARSING, EXECUTING, PAUSED, ABORTED
}

enum class TokenType {
    DEFINE, WHILE, ENDWHILE, FOR, TO, ENDFOR, IF, ELSE, ENDIF,
    IDENTIFIER, VAR_REF, NUMBER, STRING_LITERAL,
    OPERATOR_EQ, OPERATOR_LT, OPERATOR_GT, OPERATOR_EQEQ, OPERATOR_NEQ,
    OPERATOR_PLUS, OPERATOR_MINUS, OPERATOR_MUL, OPERATOR_DIV,
    RAW_DUCKY, NEWLINE, EOF
}

data class Token(val type: TokenType, val value: String, val line: Int)


sealed class Expr {
    data class Num(val value: Double) : Expr()
    data class Str(val value: String) : Expr()
    data class Var(val name: String) : Expr()
    data class BinaryOp(val left: Expr, val op: TokenType, val right: Expr) : Expr()
}

sealed class ASTNode {
    data class Block(val statements: List<ASTNode>) : ASTNode()
    data class Define(val varName: String, val expr: Expr) : ASTNode()
    data class While(val condition: Expr, val body: Block) : ASTNode()
    data class For(val varName: String, val start: Expr, val end: Expr, val body: Block) : ASTNode()
    data class If(val condition: Expr, val trueBody: Block, val falseBody: Block?) : ASTNode()
    data class RawDucky(val raw: String) : ASTNode()
}


class Lexer(private val code: String) {
    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        val lines = code.split("\n", "\r\n")

        for ((idx, line) in lines.withIndex()) {
            val lineNum = idx + 1
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.uppercase().startsWith("REM ")) continue

            val upperCmd = trimmed.substringBefore(" ").uppercase()
            val isKeyword = setOf(
                "DEFINE", "WHILE", "ENDWHILE", "FOR", "ENDFOR", "IF", "ELSE", "ENDIF"
            ).contains(upperCmd)

            if (isKeyword) {
                val regex = Regex("""(DEFINE|WHILE|ENDWHILE|FOR|TO|ENDFOR|IF|ELSE|ENDIF|==|!=|<=|>=|<|>|=|\+|-|\*|/|\$[a-zA-Z_][a-zA-Z0-9_]*|[a-zA-Z_][a-zA-Z0-9_]*|\d+(?:\.\d+)?|"[^"]*")""", RegexOption.IGNORE_CASE)
                val matches = regex.findAll(trimmed)
                for (match in matches) {
                    val v = match.value
                    val vu = v.uppercase()
                    val type = when {
                        vu in setOf("DEFINE", "WHILE", "ENDWHILE", "FOR", "TO", "ENDFOR", "IF", "ELSE", "ENDIF") -> TokenType.valueOf(vu)
                        v == "==" -> TokenType.OPERATOR_EQEQ
                        v == "!=" -> TokenType.OPERATOR_NEQ
                        v == "<" -> TokenType.OPERATOR_LT
                        v == ">" -> TokenType.OPERATOR_GT
                        v == "=" -> TokenType.OPERATOR_EQ
                        v == "+" -> TokenType.OPERATOR_PLUS
                        v == "-" -> TokenType.OPERATOR_MINUS
                        v == "*" -> TokenType.OPERATOR_MUL
                        v == "/" -> TokenType.OPERATOR_DIV
                        v.startsWith("$") -> TokenType.VAR_REF
                        v.startsWith("\"") -> TokenType.STRING_LITERAL
                        v[0].isDigit() -> TokenType.NUMBER
                        else -> TokenType.IDENTIFIER
                    }
                    tokens.add(Token(type, v, lineNum))
                }
            } else {
                tokens.add(Token(TokenType.RAW_DUCKY, trimmed, lineNum))
            }
            tokens.add(Token(TokenType.NEWLINE, "\\n", lineNum))
        }
        tokens.add(Token(TokenType.EOF, "", lines.size + 1))
        return tokens
    }
}


class Parser(private val tokens: List<Token>) {
    private var pos = 0
    private fun peek() = if (pos < tokens.size) tokens[pos] else tokens.last()
    private fun advance() = if (pos < tokens.size) tokens[pos++] else tokens.last()
    private fun consume(type: TokenType): Token? {
        if (peek().type == type) return advance()
        return null
    }
    private fun consumeNewlines() {
        while (peek().type == TokenType.NEWLINE) advance()
    }

    fun parse(): ASTNode.Block {
        val stmts = mutableListOf<ASTNode>()
        while (peek().type != TokenType.EOF) {
            consumeNewlines()
            if (peek().type == TokenType.EOF) break
            val s = parseStatement()
            if (s != null) stmts.add(s)
            else advance()
        }
        return ASTNode.Block(stmts)
    }

    private fun parseStatement(): ASTNode? {
        return when (peek().type) {
            TokenType.DEFINE -> parseDefine()
            TokenType.WHILE -> parseWhile()
            TokenType.FOR -> parseFor()
            TokenType.IF -> parseIf()
            TokenType.RAW_DUCKY -> parseRaw()
            TokenType.ENDWHILE, TokenType.ENDFOR, TokenType.ENDIF, TokenType.ELSE -> null
            else -> { advance(); null }
        }
    }

    private fun parseRaw(): ASTNode {
        val t = advance()
        return ASTNode.RawDucky(t.value)
    }

    private fun parseDefine(): ASTNode? {
        advance()
        val varRef = consume(TokenType.VAR_REF) ?: return null
        consume(TokenType.OPERATOR_EQ) ?: return null
        val expr = parseExpression()
        return ASTNode.Define(varRef.value, expr)
    }

    private fun parseWhile(): ASTNode? {
        advance()
        val condition = parseExpression()
        consumeNewlines()
        val block = parseBlock(setOf(TokenType.ENDWHILE))
        consume(TokenType.ENDWHILE)
        return ASTNode.While(condition, block)
    }

    private fun parseFor(): ASTNode? {
        advance()
        val varRef = consume(TokenType.VAR_REF) ?: return null
        consume(TokenType.OPERATOR_EQ) ?: return null
        val start = parseExpression()
        consume(TokenType.TO) ?: return null
        val end = parseExpression()
        consumeNewlines()
        val block = parseBlock(setOf(TokenType.ENDFOR))
        consume(TokenType.ENDFOR)
        return ASTNode.For(varRef.value, start, end, block)
    }

    private fun parseIf(): ASTNode? {
        advance()
        val condition = parseExpression()
        consumeNewlines()
        val trueBlock = parseBlock(setOf(TokenType.ELSE, TokenType.ENDIF))
        var falseBlock: ASTNode.Block? = null
        if (peek().type == TokenType.ELSE) {
            advance()
            consumeNewlines()
            falseBlock = parseBlock(setOf(TokenType.ENDIF))
        }
        consume(TokenType.ENDIF)
        return ASTNode.If(condition, trueBlock, falseBlock)
    }

    private fun parseBlock(endTokens: Set<TokenType>): ASTNode.Block {
        val stmts = mutableListOf<ASTNode>()
        while (peek().type != TokenType.EOF && !endTokens.contains(peek().type)) {
            consumeNewlines()
            if (peek().type == TokenType.EOF || endTokens.contains(peek().type)) break
            val s = parseStatement()
            if (s != null) stmts.add(s)
            else advance()
        }
        return ASTNode.Block(stmts)
    }

    private fun parseExpression(): Expr {
        return parseLogical()
    }

    private fun parseLogical(): Expr {
        var left = parseTerm()
        while (peek().type in setOf(TokenType.OPERATOR_EQEQ, TokenType.OPERATOR_NEQ, TokenType.OPERATOR_LT, TokenType.OPERATOR_GT)) {
            val op = advance().type
            val right = parseTerm()
            left = Expr.BinaryOp(left, op, right)
        }
        return left
    }

    private fun parseTerm(): Expr {
        var left = parseFactor()
        while (peek().type in setOf(TokenType.OPERATOR_PLUS, TokenType.OPERATOR_MINUS)) {
            val op = advance().type
            val right = parseFactor()
            left = Expr.BinaryOp(left, op, right)
        }
        return left
    }

    private fun parseFactor(): Expr {
        var left = parseAtomic()
        while (peek().type in setOf(TokenType.OPERATOR_MUL, TokenType.OPERATOR_DIV)) {
            val op = advance().type
            val right = parseAtomic()
            left = Expr.BinaryOp(left, op, right)
        }
        return left
    }

    private fun parseAtomic(): Expr {
        val t = advance()
        return when (t.type) {
            TokenType.NUMBER -> Expr.Num(t.value.toDoubleOrNull() ?: 0.0)
            TokenType.STRING_LITERAL -> Expr.Str(t.value.removeSurrounding("\""))
            TokenType.VAR_REF -> Expr.Var(t.value)
            else -> Expr.Num(0.0)
        }
    }
}


class DuckyInterpreter(private val btManager: BluetoothHidManager) {
    
    val state = MutableStateFlow(InterpreterState.IDLE)
    private var isAborted = false

    private suspend fun simulateHumanJitter(baseDelayMs: Long = 12, varianceMs: Long = 10) {
        val jitter = (java.util.Random().nextGaussian() * varianceMs).toLong()
        val delayTime = (baseDelayMs + jitter).coerceAtLeast(0L)
        if (delayTime > 0) delay(delayTime)
    }

    fun abort() {
        isAborted = true
        state.value = InterpreterState.ABORTED
    }

    suspend fun execute(script: String, onLog: (String) -> Unit, languageId: Int = 0) {
        isAborted = false
        state.value = InterpreterState.PARSING
        try {
            onLog("[Diagnostic VM] Generating Lexical Tokens...")
            val lexer = Lexer(script)
            val tokens = lexer.tokenize()

            onLog("[Diagnostic VM] Building Abstract Syntax Tree...")
            val parser = Parser(tokens)
            val ast = parser.parse()

            state.value = InterpreterState.EXECUTING
            onLog("[Diagnostic VM] Compiling and Executing Payload Tree...")

            val environment = mutableMapOf<String, Any>()
            evalBlock(ast, environment, onLog, languageId)

            if (!isAborted) {
                state.value = InterpreterState.IDLE
                onLog("[Diagnostic VM] Process Terminated Gracefully.")
            }
        } catch (e: Exception) {
            state.value = InterpreterState.ABORTED
            onLog("[System Error] VM Crash: ${e.message}")
        }
    }

    private suspend fun evalBlock(block: ASTNode.Block, env: MutableMap<String, Any>, onLog: (String) -> Unit, languageId: Int) {
        for (stmt in block.statements) {
            if (isAborted) break
            evalStatement(stmt, env, onLog, languageId)
        }
    }

    private suspend fun evalStatement(node: ASTNode, env: MutableMap<String, Any>, onLog: (String) -> Unit, languageId: Int) {
        yield()
        when (node) {
            is ASTNode.Block -> evalBlock(node, env, onLog, languageId)
            is ASTNode.Define -> {
                val value = evalExpr(node.expr, env)
                env[node.varName] = value
                onLog("[Mem] ${node.varName} -> $value")
            }
            is ASTNode.While -> {
                while (!isAborted && isTruthy(evalExpr(node.condition, env))) {
                    evalBlock(node.body, env, onLog, languageId)
                }
            }
            is ASTNode.For -> {
                val start = evalExpr(node.start, env)
                val end = evalExpr(node.end, env)
                val s = if (start is Double) start.toInt() else 0
                val e = if (end is Double) end.toInt() else 0
                var i = s
                val step = if (s <= e) 1 else -1
                while (!isAborted && ((step == 1 && i <= e) || (step == -1 && i >= e))) {
                    env[node.varName] = i.toDouble()
                    evalBlock(node.body, env, onLog, languageId)
                    i += step
                }
            }
            is ASTNode.If -> {
                val cond = evalExpr(node.condition, env)
                if (isTruthy(cond)) {
                    evalBlock(node.trueBody, env, onLog, languageId)
                } else if (node.falseBody != null) {
                    evalBlock(node.falseBody, env, onLog, languageId)
                }
            }
            is ASTNode.RawDucky -> {
                var compiledLine = node.raw
                val sortedKeys = env.keys.sortedByDescending { it.length }
                for (k in sortedKeys) {
                    val v = env[k]
                    val strVal = if (v is Double && v % 1 == 0.0) v.toLong().toString() else v.toString()
                    compiledLine = compiledLine.replace(k, strVal)
                }
                executeRawDuckyCommand(compiledLine, onLog, languageId)
            }
        }
    }

    private fun isTruthy(value: Any): Boolean {
        return when (value) {
            is Double -> value > 0.0
            is Boolean -> value
            is String -> value.isNotEmpty()
            else -> false
        }
    }

    private fun evalExpr(expr: Expr, env: MutableMap<String, Any>): Any {
        return when (expr) {
            is Expr.Num -> expr.value
            is Expr.Str -> expr.value
            is Expr.Var -> env[expr.name] ?: 0.0
            is Expr.BinaryOp -> {
                val left = evalExpr(expr.left, env)
                val right = evalExpr(expr.right, env)

                when (expr.op) {
                    TokenType.OPERATOR_PLUS -> {
                        if (left is String || right is String) left.toString() + right.toString()
                        else (left as? Double ?: 0.0) + (right as? Double ?: 0.0)
                    }
                    TokenType.OPERATOR_MINUS -> (left as? Double ?: 0.0) - (right as? Double ?: 0.0)
                    TokenType.OPERATOR_MUL -> (left as? Double ?: 0.0) * (right as? Double ?: 0.0)
                    TokenType.OPERATOR_DIV -> {
                        val r = (right as? Double ?: 1.0)
                        if (r == 0.0) 0.0 else (left as? Double ?: 0.0) / r
                    }
                    TokenType.OPERATOR_EQEQ -> left == right
                    TokenType.OPERATOR_NEQ -> left != right
                    TokenType.OPERATOR_LT -> (left as? Double ?: 0.0) < (right as? Double ?: 0.0)
                    TokenType.OPERATOR_GT -> (left as? Double ?: 0.0) > (right as? Double ?: 0.0)
                    else -> 0.0
                }
            }
        }
    }

    private suspend fun executeRawDuckyCommand(line: String, onLog: (String) -> Unit, languageId: Int) {
        val hidParser = HID(languageId)
        hidParser.parse(line)

        for (cmd in hidParser.cmd) {
            if (isAborted) break
            val trimCmd = cmd.trim()
            if (trimCmd.isEmpty()) continue

            try {
                if (trimCmd.startsWith("send_keys \"\\\\x")) {
                    val hexRegex = Regex("\\\\\\\\x([0-9a-fA-F]{2})")
                    val matches = hexRegex.findAll(trimCmd).toList()
                    if (matches.size >= 8) {
                        val report = ByteArray(8)
                        for (i in 0 until 8) {
                            report[i] = matches[i].groupValues[1].toInt(16).toByte()
                        }
                        
                        simulateHumanJitter()
                        
                        if (btManager.isConnected.value && btManager.isRegistered.value) {
                            btManager.sendRaw(1, report)
                        } else {
                            NativeIOManager.writeBytesNative("/dev/hidg0", report)
                        }
                    }
                } else if (trimCmd.startsWith("send_mouse \"\\\\x")) {
                    val hexRegex = Regex("\\\\\\\\x([0-9a-fA-F]{2})")
                    val matches = hexRegex.findAll(trimCmd).toList()
                    if (matches.size >= 4) {
                        val report = ByteArray(4)
                        for (i in 0 until 4) {
                            report[i] = matches[i].groupValues[1].toInt(16).toByte()
                        }
                        
                        if (btManager.isConnected.value && btManager.isRegistered.value) {
                            btManager.sendRaw(2, report)
                        } else {
                            NativeIOManager.writeBytesNative("/dev/hidg1", report)
                        }
                    }
                } else if (trimCmd.startsWith("sleep ")) {
                    val timeStr = trimCmd.substring(6)
                    val timeSecs = timeStr.toFloatOrNull() ?: 0f
                    if (timeSecs > 0) delay((timeSecs * 1000).toLong())
                } else if (trimCmd.startsWith("echo ")) {
                    val msg = trimCmd.substring(5).removeSurrounding("\"")
                    onLog("> $msg")
                }
            } catch (e: Exception) {
                onLog("[VM Fault] Invalid Stream Bytecode: ${e.message}")
            }
        }
    }
}
