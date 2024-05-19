.data
	$msg1: .asciiz " * "
	$msg2: .asciiz "\n"
	$msg3: .asciiz " = "
.text
j main

FUNC_mostraTaulaMultiplicar:
	sub $sp, $sp, 20
	sw $fp, 0($sp)
	sw $ra, 4($sp)
	sw $a0, 8($sp)
	move $fp, $sp
	li $t0, 25
	li $t1, 0
	LOOP0:
	li $t3, 10
	sle $t2, $t0, $t3
	xori $t4, $t2, 0x1
	bne $t4, $zero, L1
	mul $t4, $a0, $t0
	move $t1, $t4
	li $v0, 1
	move $a0, $a0
	syscall
	lw $a0, 8($sp)
	
	li $v0, 4
	la $a0, $msg1
	syscall
	lw $a0, 8($sp)
	
	li $v0, 1
	move $a0, $t0
	syscall
	lw $a0, 8($sp)
	
	li $v0, 4
	la $a0, $msg3
	syscall
	lw $a0, 8($sp)
	
	li $v0, 1
	move $a0, $t1
	syscall
	lw $a0, 8($sp)
	
	li $v0, 4
	la $a0, $msg2
	syscall
	lw $a0, 8($sp)
	
	addi $t5, $t0, 1
	move $t0, $t5
	j LOOP0
	L1:
	move $sp, $fp
	lw $fp, 0($sp)
	lw $ra, 4($sp)
	addi $sp, $sp, 8
	jr $ra
# END_FUNC

main:
	sub $sp, $sp, 4
	move $fp, $sp
	li $t0, 5
	move $a0, $t0
	li $t0, 10
	move $a1, $t0
	lw $t0, 0($fp)
	li $t0, 9
	add $t0, $t0, $t0
	move $a2, $t0
	sw $t0, 0($fp)
	jal FUNC_mostraTaulaMultiplicar
	move $t0, $v0
	
	
	li $v0, 10
	syscall # Finalitzem el programa
	addi $sp, $sp, 4
