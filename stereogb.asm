INCLUDE "hardware.inc"

SECTION "Timer interrupt", ROM0[$50]
TimerInterrupt:
    nop
	jp StereoGB
	nop
	nop
	nop
	nop

SECTION "Header", ROM0[$100]

EntryPoint:
	di
	jp Start

REPT $150 - $104
	db 0
ENDR

SECTION "Game code", ROM0[$150]

Start:
	di
    nop
    nop
    xor a
    ldh [rNR52], a
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    ld a, $80
    ldh [rNR52], a
    nop
    nop
    nop
    xor a
    ldh [rIE], a
	ld bc, $0001
	ld hl, $4000
	ld e, $0F
	xor a
	ldh [rNR10], a
	ld a, $12
	ldh [rNR51], a
	ld a, $E0
	ldh [rNR13], a
	ldh [rNR23], a
	ld a, $F0
	ldh [rNR12], a
	ldh [rNR22], a
	ld a, $C0
	ldh [rNR11], a
	ldh [rNR21], a
	ld a, $83
    ldh [rNR14], a
    ldh [rNR24], a
	ld a, $77
	ldh [rNR50], a
	ld a, 000 ;timer divider
	ldh [rTMA], a
	ld a, %00000101
	ldh [rTAC], a
	ld a, $04
	ldh [rIE], a
	ei

waitforint:
	nop
	nop
	nop
	jr waitforint

StereoGB:
	ld a, [hli]
	ld d, a
	or e
	ldh [rNR12], a
	swap d
	ld a, d
	or e
	ldh [rNR22], a
	ld a, $80
	ldh [rNR14], a
	ldh [rNR24], a
	bit 7, h
	jr z, sampleEnd
	ld h, $40
	inc bc
	ld a, c
	ld [$2000], a
	ld a, b
	ld [$3000], a
sampleEnd:
	reti

lockup:
	nop
	nop
	nop
	nop
	jr lockup
SECTION "Additional lockup", ROM0[$0600]
	jp lockup