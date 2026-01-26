import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:sm_fitrus/sm_fitrus.dart';
import '../theme/app_theme.dart';

/// User Input Form for Body Fat Measurement Parameters
class UserInputForm extends StatefulWidget {
  final Function(UserInputData) onSubmit;
  final VoidCallback? onCancel;
  final bool isLoading;
  final int progress;
  final String? statusMessage;

  const UserInputForm({
    super.key,
    required this.onSubmit,
    this.onCancel,
    this.isLoading = false,
    this.progress = 0,
    this.statusMessage,
  });

  @override
  State<UserInputForm> createState() => _UserInputFormState();
}

class _UserInputFormState extends State<UserInputForm> {
  final _formKey = GlobalKey<FormState>();

  // Controllers
  final _heightController = TextEditingController(text: '165');
  final _weightController = TextEditingController(text: '55.5');

  // Form values
  FitrusGender _gender = FitrusGender.male;
  DateTime _dob = DateTime(1990, 12, 3);

  @override
  void dispose() {
    _heightController.dispose();
    _weightController.dispose();
    super.dispose();
  }

  Future<void> _selectDate() async {
    final picked = await showDatePicker(
      context: context,
      initialDate: _dob,
      firstDate: DateTime(1920),
      lastDate: DateTime.now(),
      builder: (context, child) {
        return Theme(
          data: Theme.of(context).copyWith(
            colorScheme: const ColorScheme.dark(
              primary: AppTheme.primaryBlue,
              surface: AppTheme.backgroundCard,
            ),
          ),
          child: child!,
        );
      },
    );
    if (picked != null) {
      setState(() => _dob = picked);
    }
  }

  void _submit() {
    if (_formKey.currentState?.validate() ?? false) {
      final heightCm = double.tryParse(_heightController.text) ?? 0;
      final weightKg = double.tryParse(_weightController.text) ?? 0;

      widget.onSubmit(UserInputData(
        gender: _gender,
        dob: _dob,
        heightCm: heightCm,
        weightKg: weightKg,
      ));
    }
  }

  @override
  Widget build(BuildContext context) {
    // Button is enabled when not loading
    final canStart = !widget.isLoading;

    return GlassCard(
      child: Form(
        key: _formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Section Title
            const Row(
              children: [
                Icon(Icons.person_outline,
                    color: AppTheme.primaryBlue, size: 24),
                SizedBox(width: 12),
                Text(
                  'User Information',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.w600,
                    color: AppTheme.textPrimary,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),

            // Gender Selection
            _buildSectionLabel('Gender'),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: _GenderButton(
                    label: 'Male',
                    icon: Icons.male,
                    isSelected: _gender == FitrusGender.male,
                    onTap: () => setState(() => _gender = FitrusGender.male),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _GenderButton(
                    label: 'Female',
                    icon: Icons.female,
                    isSelected: _gender == FitrusGender.female,
                    onTap: () => setState(() => _gender = FitrusGender.female),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),

            // Date of Birth
            _buildSectionLabel('Date of Birth'),
            const SizedBox(height: 8),
            InkWell(
              onTap: _selectDate,
              borderRadius: BorderRadius.circular(12),
              child: Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
                decoration: BoxDecoration(
                  color: AppTheme.backgroundDark,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(
                      color: AppTheme.textSecondary.withOpacity(0.3)),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.calendar_today,
                        color: AppTheme.textSecondary, size: 20),
                    const SizedBox(width: 12),
                    Text(
                      '${_dob.year}/${_dob.month.toString().padLeft(2, '0')}/${_dob.day.toString().padLeft(2, '0')}',
                      style: const TextStyle(
                          color: AppTheme.textPrimary, fontSize: 16),
                    ),
                    const Spacer(),
                    const Icon(Icons.arrow_drop_down,
                        color: AppTheme.textSecondary),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 20),

            // Height & Weight Row
            Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      _buildSectionLabel('Height (cm)'),
                      const SizedBox(height: 8),
                      TextFormField(
                        controller: _heightController,
                        keyboardType: const TextInputType.numberWithOptions(
                            decimal: true),
                        inputFormatters: [
                          FilteringTextInputFormatter.allow(
                              RegExp(r'^\d*\.?\d*$')),
                        ],
                        style: const TextStyle(color: AppTheme.textPrimary),
                        decoration: InputDecoration(
                          filled: true,
                          fillColor: AppTheme.backgroundDark,
                          prefixIcon: const Icon(Icons.height,
                              color: AppTheme.textSecondary),
                          hintText: 'e.g. 165',
                          hintStyle: TextStyle(
                              color: AppTheme.textSecondary.withOpacity(0.5)),
                        ),
                        validator: (value) {
                          final v = double.tryParse(value ?? '');
                          if (v == null || v <= 0 || v > 300) {
                            return 'Invalid height';
                          }
                          return null;
                        },
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      _buildSectionLabel('Weight (kg)'),
                      const SizedBox(height: 8),
                      TextFormField(
                        controller: _weightController,
                        keyboardType: const TextInputType.numberWithOptions(
                            decimal: true),
                        inputFormatters: [
                          FilteringTextInputFormatter.allow(
                              RegExp(r'^\d*\.?\d*$')),
                        ],
                        style: const TextStyle(color: AppTheme.textPrimary),
                        decoration: InputDecoration(
                          filled: true,
                          fillColor: AppTheme.backgroundDark,
                          prefixIcon: const Icon(Icons.monitor_weight_outlined,
                              color: AppTheme.textSecondary),
                          hintText: 'e.g. 55.5',
                          hintStyle: TextStyle(
                              color: AppTheme.textSecondary.withOpacity(0.5)),
                        ),
                        validator: (value) {
                          final v = double.tryParse(value ?? '');
                          if (v == null || v <= 0 || v > 500) {
                            return 'Invalid weight';
                          }
                          return null;
                        },
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 28),

            // Start/Cancel Button
            if (widget.isLoading && widget.onCancel != null) ...[
              // Show Status and Progress
              if (widget.statusMessage != null)
                Padding(
                  padding: const EdgeInsets.only(bottom: 8.0),
                  child: Text(
                    widget.statusMessage!,
                    style: TextStyle(
                      color: AppTheme.textSecondary.withOpacity(0.8),
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
              if (widget.progress > 0)
                Padding(
                  padding: const EdgeInsets.only(bottom: 16.0),
                  child: ClipRRect(
                    borderRadius: BorderRadius.circular(8),
                    child: LinearProgressIndicator(
                      value: widget.progress / 100.0,
                      backgroundColor: AppTheme.backgroundDark,
                      color: AppTheme.primaryBlue,
                      minHeight: 8,
                    ),
                  ),
                ),

              // Show Cancel button during measurement
              SizedBox(
                width: double.infinity,
                height: 56,
                child: ElevatedButton.icon(
                  onPressed: widget.onCancel,
                  icon: const Icon(Icons.close),
                  label: const Text(
                    'Cancel Measurement',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  style: ElevatedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 16),
                    backgroundColor: AppTheme.accentOrange,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                    elevation: 4,
                  ),
                ),
              )
            ] else
              // Show Start button when not measuring
              SizedBox(
                width: double.infinity,
                height: 56,
                child: ElevatedButton(
                  onPressed: canStart ? _submit : null,
                  style: ElevatedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 16),
                    backgroundColor:
                        canStart ? AppTheme.primaryBlue : Colors.grey,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                    elevation: canStart ? 4 : 0,
                  ),
                  child: widget.isLoading
                      ? const SizedBox(
                          height: 24,
                          width: 24,
                          child: CircularProgressIndicator(
                            color: Colors.white,
                            strokeWidth: 2,
                          ),
                        )
                      : const Text(
                          'Start Measurement',
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                ),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionLabel(String label) {
    return Text(
      label,
      style: const TextStyle(
        color: AppTheme.textSecondary,
        fontSize: 13,
        fontWeight: FontWeight.w500,
      ),
    );
  }
}

/// Gender Selection Button
class _GenderButton extends StatelessWidget {
  final String label;
  final IconData icon;
  final bool isSelected;
  final VoidCallback onTap;

  const _GenderButton({
    required this.label,
    required this.icon,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(vertical: 14),
        decoration: BoxDecoration(
          color: isSelected
              ? AppTheme.primaryBlue.withOpacity(0.2)
              : AppTheme.backgroundDark,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: isSelected
                ? AppTheme.primaryBlue
                : AppTheme.textSecondary.withOpacity(0.3),
            width: isSelected ? 2 : 1,
          ),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon,
              color: isSelected ? AppTheme.primaryBlue : AppTheme.textSecondary,
              size: 22,
            ),
            const SizedBox(width: 8),
            Text(
              label,
              style: TextStyle(
                color:
                    isSelected ? AppTheme.primaryBlue : AppTheme.textSecondary,
                fontWeight: isSelected ? FontWeight.w600 : FontWeight.w400,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Data class for user input
class UserInputData {
  final FitrusGender gender;
  final DateTime dob;
  final double heightCm;
  final double weightKg;

  UserInputData({
    required this.gender,
    required this.dob,
    required this.heightCm,
    required this.weightKg,
  });

  /// Returns DOB in yyyyMMdd format
  String get birthString =>
      '${dob.year}${dob.month.toString().padLeft(2, '0')}${dob.day.toString().padLeft(2, '0')}';
}
