package backEnd;

//https://github.com/ffcabbar/MIPS-Assembly-Language-Examples/blob/master

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class TACToRISCConverter {
    private static int tempRegCounter = 0;
    private final String MIPS_FILE_PATH;

    public TACToRISCConverter(String path) {
        this.MIPS_FILE_PATH = path;
    }

    public void convertTAC(LinkedHashMap<String, TACBlock> blocks){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, TACBlock> blockEntry : blocks.entrySet()) {
            System.out.println(blockEntry.getKey());
            sb.append(blockEntry.getKey()); //Afegim el nom Ex: main:
            for (TACEntry entry : blockEntry.getValue().getEntries()) {
                sb.append(translateToMIPS(entry));
                System.out.println(entry);
            }
        }

        // Ecribirlo en el fichero de texto
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MIPS_FILE_PATH))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            System.out.println("Error Mips File: " + e.getMessage());
            System.exit(0);
        }
    }

    private String generateTempReg() {
        String tempRegName = "t" + tempRegCounter;
        tempRegCounter++;
        return tempRegName;
    }

    private String translateToMIPS(TACEntry entry) {
        return switch (entry.getType()) {
            case Type.ADD, Type.SUB, Type.MUL, Type.DIV -> processOperation(entry);
            case Type.AND, Type.OR, Type.EQ, Type.NE, Type.LT, Type.LE, Type.GT, Type.GE -> processCondition(entry);
            case Type.CONDITION -> processConditional(entry);
            case Type.CALL, Type.CALL_EXP -> processCall(entry);
            case Type.EQU -> processAssignment(entry);
            case Type.PARAM -> processParameter(entry);
            case Type.RET -> processReturn(entry);
            default -> "";
        };
    }

    private String varOrLit(String operand) {
        try {
            // Intentem convertir l'operand a un Double
            Double.parseDouble(operand);
            // Si no es llança una excepció, significa que operand és un número vàlid
            return operand;
        } catch (NumberFormatException e) {
            // Si es llança NumberFormatException, significa que operand no és un número
            return "$" + operand;
        }
    }

    private String processOperation(TACEntry entry) {
        return switch (entry.getOperation()) {
            case "+" -> // add $resultat $operand1 $operand2
                    "add " + "$" + entry.getDestination() + "," + varOrLit(entry.getOperand1()) + "," + entry.getOperand2() + "\n";
            case "-" -> //sub $resultat $operand1 $operand2
                    "sub " + "$" + entry.getDestination() + "," + varOrLit(entry.getOperand1()) + "," + entry.getOperand2() + "\n";
            case "*" ->//mult
                    "mult " + varOrLit(entry.getOperand1()) + "," + varOrLit(entry.getOperand2()) + "\n";
            case "/" ->//div
                    "div " + varOrLit(entry.getOperand1()) + "," + varOrLit(entry.getOperand2()) + "\n";
            default -> "";
        };
    }

    private String processCondition(TACEntry entry){
        switch (entry.getOperation()){
            case "!":
                //return nor $t0, $t1, $zero.
            case "AND": // and
                return "and " + entry.getDestination() + ", " + varOrLit(entry.getOperand1()) + ", " + varOrLit(entry.getOperand2()) + "\n";
            case "OR": // or
                return "or " + entry.getDestination() + ", " + varOrLit(entry.getOperand1()) + ", " + varOrLit(entry.getOperand2()) + "\n";
            case "==": // beq
                return "seq " + entry.getDestination() + ", " + varOrLit(entry.getOperand1())  + "," + varOrLit(entry.getOperand2()) + "\n";
            case "!=": // bne
                return "sne " + entry.getDestination() + ", " + varOrLit(entry.getOperand1()) + "," + varOrLit(entry.getOperand2()) + "\n";
            case "LOWER": // slt (set on less than)
                return "slt " + entry.getDestination() + ", " + varOrLit(entry.getOperand1()) + ", " + varOrLit(entry.getOperand2()) + "\n";
            case "LOWER_EQUAL": // slt and beq combination to simulate 'less than or equal'
                String tempReg = generateTempReg();
                String leResult = "slt " + tempReg + ", " + varOrLit(entry.getOperand1()) + ", " + varOrLit(entry.getOperand2()) + "\n" +
                        "xori " + tempReg + ", " + tempReg + ", 1\n" + // invert the result of slt
                        "andi " + entry.getDestination() + ", " + tempReg + ", 1"  + "\n"; // store result in destination
                tempRegCounter--;
                return leResult;
            case "GREATER": // slt (set on less than) with swapped operands to simulate 'greater than'
                return "sgt " + entry.getDestination() + ", " + varOrLit(entry.getOperand2()) + ", " + varOrLit(entry.getOperand1())  + "\n";
            case "GREATER_EQUAL": // slt and beq combination to simulate 'greater than or equal'
                String tempRegGE = generateTempReg();
                String geResult = "sgt " + tempRegGE + ", " + varOrLit(entry.getOperand2()) + ", " + varOrLit(entry.getOperand1()) + "\n" +
                        "xori " + tempRegGE + ", " + tempRegGE + ", 1\n" + // invert the result of slt
                        "andi " + entry.getDestination() + ", " + tempRegGE + ", 1"  + "\n"; // store result in destination
                tempRegCounter--;
                return geResult;
        }
        return "";
    }

    private String processConditional(TACEntry entry){
        //negar la condició nor $t0, $t1, $zero, afegir-ho a un registre temporal i posar que el if comprovi això

        return "";
    }

    private String processCall(TACEntry entry) {
        return "jal " + varOrLit(entry.getOperand1()) + "\n";
    }

    private String processAssignment(TACEntry entry){
        // move $dest,$src
        return "move $" + entry.getDestination() +"," + varOrLit(entry.getOperand1()) + "\n";
    }

    private String processParameter(TACEntry entry) {
        return "addi $a0, $zero, " + entry.getOperand1() + "\n";
    }

    private String processReturn(TACEntry entry){
        StringBuilder stringBuilder = new StringBuilder();

        String toReturnRegis = "move $ra, " + varOrLit(entry.getOperand2());
        stringBuilder.append(toReturnRegis).append("\n");
        String returnLine = "jr $ra";
        stringBuilder.append(returnLine).append("\n"); // Afegim un salt de línia per a millor format del resultat

        return stringBuilder.toString();
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