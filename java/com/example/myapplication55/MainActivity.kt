package com.example.myapplication55

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    // Define UI elements
    private lateinit var altitudeEditText: EditText
    private lateinit var MEditText: EditText
    private lateinit var T3EditText: EditText
    private lateinit var AFRatioEditText: EditText
    private lateinit var etaCompressorEditText: EditText
    private lateinit var etaTurbineEditText: EditText
    private lateinit var etaIntakeEditText: EditText
    private lateinit var etaNozzleEditText: EditText
    private lateinit var etaMechanicalEditText: EditText
    private lateinit var etaCombustionEditText: EditText
    private lateinit var combustionPressureLossEditText: EditText
    private lateinit var PiCEditText: EditText
    private lateinit var calorificValueEditText: EditText
    private lateinit var calculateButton: Button
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        altitudeEditText = findViewById(R.id.altitudeEditText)
        MEditText = findViewById(R.id.MEditText)
        T3EditText = findViewById(R.id.T3EditText)
        AFRatioEditText = findViewById(R.id.AFRatioEditText)
        etaCompressorEditText = findViewById(R.id.etaCompressorEditText)
        etaTurbineEditText = findViewById(R.id.etaTurbineEditText)
        etaIntakeEditText = findViewById(R.id.etaIntakeEditText)
        etaNozzleEditText = findViewById(R.id.etaNozzleEditText)
        etaMechanicalEditText = findViewById(R.id.etaMechanicalEditText)
        etaCombustionEditText = findViewById(R.id.etaCombustionEditText)
        combustionPressureLossEditText = findViewById(R.id.combustionPressureLossEditText)
        PiCEditText = findViewById(R.id.PiCEditText)
        calorificValueEditText = findViewById(R.id.calorificValueEditText)
        calculateButton = findViewById(R.id.calculateButton)
        resultTextView = findViewById(R.id.resultTextView)

        // Set a click listener for the calculate button
        calculateButton.setOnClickListener {
            calculateJetEngineSimulation()
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun calculateJetEngineSimulation() {
        // Retrieve user inputs
        val altitude = altitudeEditText.text.toString().toDouble()
        val M = MEditText.text.toString().toDouble()
        val T3 = T3EditText.text.toString().toDouble()
        val AF_ratio = AFRatioEditText.text.toString().toDouble()
        val etaCompressor = etaCompressorEditText.text.toString().toDouble()
        val etaTurbine = etaTurbineEditText.text.toString().toDouble()
        val etaIntake = etaIntakeEditText.text.toString().toDouble()
        val etaNozzle = etaNozzleEditText.text.toString().toDouble()
        val etaMechanical = etaMechanicalEditText.text.toString().toDouble()
        val etaCombustion = etaCombustionEditText.text.toString().toDouble()
        val combustionPressureLoss = combustionPressureLossEditText.text.toString().toDouble() / 100.0
        val PiC = PiCEditText.text.toString().toDouble()
        val calorificValue = calorificValueEditText.text.toString().toDouble()

        // Thermodynamic properties:
        val gammaC = 1.4
        val gammaH = 1.33
        val r = 287
        val Cp = 1.005
        val Cpg = 1.148

        // Turbine Prssure ratio:
        var pi_t = 1.0 / (1.0 - combustionPressureLoss)

        // Isentropic relations:
        val P0 = 1.01325 * (1 - 0.0065 * altitude / 288.15).pow(5.2561)
        val t0 = 288.16 - (0.0065 * altitude)

        val a = sqrt(gammaC * r * t0)
        val Vi = M * a

        // Diffuser:
        val P1 = P0 * (1 + (etaIntake * ((Vi.pow(2)) / (2 * Cp * 1000 * t0)))).pow(gammaC / (gammaC - 1))
        val T1 = t0 + ((Vi.pow(2)) / (2 * Cp * 1000))

        // Compressor:
        val P2 = PiC * P1
        val T2 = T1 + ((T1 * ((PiC.pow((gammaC - 1) / gammaC)) - 1)) / etaCompressor)

        // Combustion Chamber:
        val P3 = P2 * (1 - combustionPressureLoss)

        // Turbine power absorbed by Turbine = Work done by the Compressor:
        val T4 = T3 - ((Cp * (T2 - T1)) / (etaMechanical * Cpg))
        val T4s = T3 - ((T3 - T4) / etaTurbine)
        val P4 = P3 * ((T4s / T3).pow(gammaH / (gammaH - 1)))

        // Calculate nozzle pressure ratio:
        val Pn = P4 / P0

        // Calculate critical pressure ratio:
        val Pc = P4 * (1 - ((gammaH - 1) / (gammaH + 1)) / etaNozzle).pow(gammaH / (gammaH - 1))
        val Tc = T4 * (2 / (gammaH + 1))
        val Pcn = P4 / Pc

        val temperatures = mutableListOf(T1, T2, T3, T4)
        val entropies = mutableListOf<Double>()

        // Check whether the nozzle is choking:
        if (P4 / P0 > P4 / Pc) {
            val T5 = Tc
            val P5 = Pc
            val rhoE = Pc / (r * Tc)
            val Ve = sqrt(gammaH * r * Tc)

            // Calculate Specific Thrust:
            val specificThrust = (Ve - Vi) + ((Pc - P0) / (rhoE * Ve))

            // Actual Air/Fuel ratio:
            val af_ratioActual = AF_ratio / etaCombustion

            // Calculate Specific Fuel Consumption:
            val sfc = (af_ratioActual * 3600.0) / specificThrust // Convert from kg/s/N to kg/(N·hr)
            val formattedSFC = String.format("%.4f", sfc) // Format SFC to 4 decimal places

            val resultsText = """
                ${getString(R.string.ambient_pressure)}: ${"%.2f".format(P0)} ${getString(R.string.bar)}
                ${getString(R.string.ambient_temperature)}: ${"%.2f".format(t0)} ${getString(R.string.K)}
                ${getString(R.string.local_speed_of_sound)}: ${"%.2f".format(a)} ${getString(R.string.m_s)}
                ${getString(R.string.inlet_velocity_of_air)}: ${"%.2f".format(Vi)} ${getString(R.string.m_s)}
                ${getString(R.string.diffuser_outlet_pressure)}: ${"%.2f".format(P1)} ${getString(R.string.bar)}
                ${getString(R.string.diffuser_outlet_temperature)}: ${"%.2f".format(T1)} ${getString(R.string.K)}
                ${getString(R.string.compressor_outlet_pressure)}: ${"%.2f".format(P2)} ${getString(R.string.bar)}
                ${getString(R.string.compressor_outlet_temperature)}: ${"%.2f".format(T2)} ${getString(R.string.K)}
                ${getString(R.string.combustion_chamber_outlet_pressure)}: ${"%.2f".format(P3)} ${getString(R.string.bar)}
                ${getString(R.string.turbine_outlet_pressure)}: ${"%.2f".format(P4)} ${getString(R.string.bar)}
                ${getString(R.string.turbine_outlet_temperature)}: ${"%.2f".format(T4)} ${getString(R.string.K)}
                ${getString(R.string.nozzle_pressure_ratio)}: ${"%.2f".format(Pn)}
                ${getString(R.string.nozzle_critical_pressure)}: ${"%.2f".format(Pc)} ${getString(R.string.bar)}
                ${getString(R.string.nozzle_critical_temperature)}: ${"%.2f".format(Tc)} ${getString(R.string.K)}
                ${getString(R.string.nozzle_critical_pressure_ratio)}: ${"%.2f".format(Pcn)}
                ${getString(R.string.actual_air_fuel_ratio)}: ${"%.2f".format(af_ratioActual)}
                ${getString(R.string.specific_fuel_consumption)}: ${formattedSFC} kg/(N·hr)
                ${getString(R.string.specific_thrust)}: ${"%.2f".format(specificThrust)} N/kg/s
            """.trimIndent()

            // Display the results in the resultTextView:
            resultTextView.text = resultsText
        } else {
            // Nozzle is not choking
            val resultsText = getString(R.string.nozzle_not_choked)
            resultTextView.text = resultsText
        }
    }
}