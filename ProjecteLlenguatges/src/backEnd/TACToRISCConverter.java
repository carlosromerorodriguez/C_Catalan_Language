package backEnd;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TACToRISCConverter {
    private final String MIPS_FILE_PATH;
    private Stack<String> freeRegisters;
    private Map<String, String> variableToRegisterMap;
    private Map<String, Integer> variableToStackOffsetMap;
    private Stack<String> usedRegisters;
    private int stackOffset;

    public TACToRISCConverter(String path) {
        this.MIPS_FILE_PATH = path;
        this.freeRegisters = new Stack<>();
        this.variableToRegisterMap = new HashMap<>();
        this.variableToStackOffsetMap = new HashMap<>();
        this.usedRegisters = new Stack<>();
        this.stackOffset = 0;
        initializeRegisters();
    }

    private void initializeRegisters() {
        // Inicializar la pila de registros temporales ($t0 - $t9)
        for (int i = 9; i >= 0; i--) {
            freeRegisters.push("$t" + i);
        }
    }

    public void convertTAC(LinkedHashMap<String, TACBlock> blocks) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, TACBlock> blockEntry : blocks.entrySet()) {
            sb.append(blockEntry.getKey()).append(":\n"); // Añadir el nombre del bloque con nueva línea
            for (TACEntry entry : blockEntry.getValue().getEntries()) {
                sb.append(translateToMIPS(entry)).append("\n");
            }
        }

        // Escribir en el fichero de texto
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MIPS_FILE_PATH))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            System.out.println("Error Mips File: " + e.getMessage());
            System.exit(0);
        }
    }

    private String translateToMIPS(TACEntry entry) {
        StringBuilder sb = new StringBuilder();

        // Cargar variables desde la pila si es necesario
        if (variableToStackOffsetMap.containsKey(entry.getOperand1())) {
            sb.append(loadFromStack(entry.getOperand1())).append("\n");
        }
        if (variableToStackOffsetMap.containsKey(entry.getOperand2())) {
            sb.append(loadFromStack(entry.getOperand2())).append("\n");
        }
        if (variableToStackOffsetMap.containsKey(entry.getDestination())) {
            sb.append(loadFromStack(entry.getDestination())).append("\n");
        }

        sb.append(switch (entry.getType()) {
            case ADD, SUB, MUL, DIV -> processOperation(entry);
            case AND, OR, EQ, NE, LT, LE, GT, GE -> processCondition(entry);
            case CONDITION -> processConditional(entry);
            case CALL, CALL_EXP -> processCall(entry);
            case EQU -> processAssignment(entry);
            case PARAM -> processParameter(entry);
            case RET -> processReturn(entry);
            case GOTO -> processGoto(entry);
            default -> "";
        });

        return sb.toString();
    }

    private String varOrReg(String operand) {
        if (variableToRegisterMap.containsKey(operand)) {
            return variableToRegisterMap.get(operand);
        } else if (isNumeric(operand)) {
            return operand;
        } else {
            String reg = allocateRegister(operand);
            variableToRegisterMap.put(operand, reg);
            return reg;
        }
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String allocateRegister(String var) {
        if (freeRegisters.isEmpty()) {
            // No hay registros libres, realizar un spill a la pila
            String spilledReg = usedRegisters.pop();
            String spillCode = spillRegisterToStack(spilledReg);
            System.out.println(spillCode); // Añadir el código de spill al output (o almacenarlo para posterior uso)
            return spilledReg;
        }
        String reg = freeRegisters.pop();
        usedRegisters.push(reg);
        return reg;
    }

    private void freeRegister(String reg) {
        if (usedRegisters.remove(reg)) {
            freeRegisters.push(reg);
        }
    }


    private String processOperation(TACEntry entry) {
        String operand1 = varOrReg(entry.getOperand1());
        String operand2 = varOrReg(entry.getOperand2());
        String destination = varOrReg(entry.getDestination());

        String mipsCode = switch (entry.getOperation()) {
            case "+" -> "add " + destination + ", " + operand1 + ", " + operand2;
            case "-" -> "sub " + destination + ", " + operand1 + ", " + operand2;
            case "*" -> "mul " + destination + ", " + operand1 + ", " + operand2;
            case "/" -> "div " + destination + ", " + operand1 + ", " + operand2;
            case "GREATER" -> "slt " + destination + ", " + operand2 + ", " + operand1; // operand1 > operand2
            default -> "";
        };

        freeRegister(operand1);
        freeRegister(operand2);
        return mipsCode;
    }


    private String processCondition(TACEntry entry) {
        String operand1 = varOrReg(entry.getOperand1());
        String operand2 = varOrReg(entry.getOperand2());
        String destination = entry.getDestination(); // Destino es una etiqueta en este caso

        String mipsCode = switch (entry.getOperation()) {
            case "&&" -> "and " + operand1 + ", " + operand1 + ", " + operand2;
            case "||" -> "or " + operand1 + ", " + operand1 + ", " + operand2;
            case "==" -> "beq " + operand1 + ", " + operand2 + ", " + destination;
            case "!=" -> "bne " + operand1 + ", " + operand2 + ", " + destination;
            case "LOWER" -> "blt " + operand1 + ", " + operand2 + ", " + destination;
            case "LOWER_EQUAL" -> "ble " + operand1 + ", " + operand2 + ", " + destination;
            case "GREATER" -> "bgt " + operand1 + ", " + operand2 + ", " + destination;
            case "GREATER_EQUAL" -> "bge " + operand1 + ", " + operand2 + ", " + destination;
            default -> "";
        };

        freeRegister(operand1);
        freeRegister(operand2);
        return mipsCode;
    }


    private String processConditional(TACEntry entry) {
        String operand1 = varOrReg(entry.getOperand1());
        String tempReg = allocateRegister("temp");
        String notCondition = "nor " + tempReg + ", " + operand1 + ", $zero\n";
        notCondition += "bne " + tempReg + ", $zero, " + entry.getDestination();
        freeRegister(tempReg);
        return notCondition;
    }

    private String processCall(TACEntry entry) {
        // Guardar els registres temporals fets servir a la pila (guardar context)
        // Al tornar de la crida, restaurar els registres temporals
        return "jal " + entry.getOperand1();
    }

    private String processAssignment(TACEntry entry) {
        String src = entry.getOperand1();
        String dest = varOrReg(entry.getDestination());

        if (isNumeric(src)) {
            return "li " + dest + ", " + src;
        } else {
            String srcReg = varOrReg(src);
            return "move " + dest + ", " + srcReg;
        }
    }

    private String processParameter(TACEntry entry) {
        String param = varOrReg(entry.getOperand1());
        return "addi $a0, $zero, " + param;
    }

    private String processReturn(TACEntry entry) {
        StringBuilder stringBuilder = new StringBuilder();
        if (entry.getOperand2() != null && !entry.getOperand2().isEmpty()) {
            stringBuilder.append("move $v0, ").append(varOrReg(entry.getOperand2())).append("\n");
        }
        stringBuilder.append("jr $ra");
        return stringBuilder.toString();
    }

    private String processGoto(TACEntry entry) {
        return "j " + entry.getDestination();
    }

    private String spillRegisterToStack(String reg) {
        int offset = stackOffset;
        variableToStackOffsetMap.put(reg, offset);
        stackOffset += 4;
        return "sw " + reg + ", " + offset + "($sp)";
    }

    private String loadFromStack(String var) {
        int offset = variableToStackOffsetMap.get(var);
        String reg = allocateRegister(var);
        return "lw " + reg + ", " + offset + "($sp)";
    }
}

/*
TAC: if a < b goto L1
MIPS: slt $t0, $a, $b  ; set on less than
      bne $t0, $zero, L1  ; branch if not equal to zero

  TAC if i >= 10 goto L2
        bge $t0, 10, L2

TAC: a = b
MIPS: move $a, $b  ; Suponiendo que a y b están en registros $a y $b

TAC: t1 = b + c
MIPS: add $t1, $b, $c  ; Nuevamente, suponiendo que b y c están en registros $b y $c

TAC: a = M[addr]
MIPS: lw $a, addr
TAC: M[addr] = b
MIPS: sw $b, addr

 */

/*

https://www.doc.ic.ac.uk/lab/secondyear/spim/node15.html
//https://github.com/ffcabbar/MIPS-Assembly-Language-Examples/blob/master
//https://web.cecs.pdx.edu/~mperkows/temp/register-allocation.pdf

while:
	beq $t1,$zero,endWhile
	rem $t3,$t1,10                                   #  123%10 = 3
	add $t2,$t2,$t3
	div $t1,$t1,10
	j while

    recursivePowerFunction:
# a0 -> baseNumber       a1 -> expNumber		v0 -> result
#	recursivePowerFunction
#	   if (expNumber == 0)
#			return 1;
#	   else
#		   return baseNumber * recursivePowerFunction(baseNumber,expNumber - 1)

	bne $a1,$zero,recursion
	li $v0,1
	jr $ra
recursion:
	addi $sp,-4
	sw $ra,0($sp)
	addi $a1,$a1,-1
	jal recursivePowerFunction
	mul $v0,$v0,$a0
	lw $ra,0($sp)
	addi $sp,4
	jr $ra


#########################################################
main:
	li $v0, 4
	la $a0, string1
	syscall

	li $v0,5
	syscall
	move $t0,$v0

	li $v0, 4
	la $a0, string2
	syscall

	li $v0,5
	syscall
	move $t1,$v0

	move $a0,$t0
	move $a1,$t1
	jal recursivePowerFunction
	move $a0,$v0
	li $v0,1
	syscall

	li $v0, 10
	syscall
 */