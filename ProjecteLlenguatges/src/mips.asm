.data
.text
j main

main:
	sub $sp, $sp, 8
	move $fp, $sp
	li $t0, 0
	li $t1, 1
	addi $t1, $t1, -1
	LOOP0:
	li $t3, 5
	slt $t2, $t1, $t3
	xori $t4, $t2, 0x1
	bne $t4, $zero, L1
	addi $t1, $t1, 1
	mul $t4, $t1, 2
	add $t5, $t0, $t4
	move $t0, $t5
	j LOOP0
	L1:
	
	li $v0, 10
	syscall # Finalitzem el programa
	addi $sp, $sp, 8
