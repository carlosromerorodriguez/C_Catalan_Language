j main

main:
	sub $sp, $sp, 12
	move $fp, $sp
	li $t0, 0
	li $t1, 10
	li $t2, 0
	LOOP0:
	sgt $t3, $t1, $t0
	xori $t4, $t3, 0x1
	bne $t4, $zero, L1
	add $t4, $t2, $t0
	move $t2, $t4
	addi $t5, $t0, 1
	move $t0, $t5
	j LOOP0
	L1:
	add $t5, $t2, $t1
	move $t2, $t5
	
	li $v0, 10
	syscall # Finalitzem el programa
	addi $sp, $sp, 12
