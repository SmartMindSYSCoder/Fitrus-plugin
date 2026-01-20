import 'package:flutter/material.dart';
import 'package:sm_fitrus/fitrus_model.dart';
import '../theme/app_theme.dart';

/// Results Card - Displays body composition measurement results
class ResultsCard extends StatelessWidget {
  final BodyFat? bodyFat;
  final bool hasError;
  final String? errorMessage;

  const ResultsCard({
    super.key,
    this.bodyFat,
    this.hasError = false,
    this.errorMessage,
  });

  @override
  Widget build(BuildContext context) {
    if (hasError) {
      return _buildErrorCard();
    }

    if (bodyFat == null || bodyFat!.fatPercentage <= 0) {
      return const SizedBox.shrink();
    }

    final isDark = Theme.of(context).brightness == Brightness.dark;
    final textColor = isDark ? AppTheme.textPrimary : AppTheme.textDark;

    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(Icons.grid_view_rounded,
                  color: AppTheme.primaryBlue, size: 24),
              const SizedBox(width: 12),
              Text(
                'Detailed Analysis',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w600,
                  color: textColor,
                ),
              ),
            ],
          ),
          const SizedBox(height: 20),
          _buildMetricsGrid(context),
        ],
      ),
    );
  }

  Widget _buildErrorCard() {
    return GlassCard(
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: AppTheme.errorRed.withOpacity(0.2),
              ),
              child: const Icon(
                Icons.error_outline,
                color: AppTheme.errorRed,
                size: 24,
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Measurement Error',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w600,
                      color: AppTheme.errorRed,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    errorMessage ?? 'An error occurred during measurement',
                    style: TextStyle(
                      fontSize: 13,
                      color: AppTheme.textSecondary.withOpacity(0.8),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMetricsGrid(BuildContext context) {
    final metrics = [
      _MetricItem(
        label: 'BMI',
        value: bodyFat!.bmi.toStringAsFixed(1),
        unit: 'kg/m²',
        icon: Icons.speed,
        color: AppTheme.primaryBlue,
      ),
      _MetricItem(
        label: 'Body Fat',
        value: bodyFat!.fatPercentage.toStringAsFixed(1),
        unit: '%',
        icon: Icons.pie_chart,
        color: AppTheme.errorRed,
      ),
      _MetricItem(
        label: 'Fat Mass',
        value: bodyFat!.fatMass.toStringAsFixed(1),
        unit: 'kg',
        icon: Icons.fitness_center,
        color: Colors.amber,
      ),
      _MetricItem(
        label: 'Muscle',
        value: bodyFat!.muscleMass.toStringAsFixed(1),
        unit: 'kg',
        icon: Icons.sports_gymnastics,
        color: AppTheme.accentGreen,
      ),
      _MetricItem(
        label: 'BMR',
        value: bodyFat!.bmr.toStringAsFixed(0),
        unit: 'kcal',
        icon: Icons.local_fire_department,
        color: AppTheme.accentOrange,
      ),
      _MetricItem(
        label: 'Water',
        value: bodyFat!.waterPercentage.toStringAsFixed(1),
        unit: '%',
        icon: Icons.water_drop,
        color: Colors.cyan,
      ),
      _MetricItem(
        label: 'Protein',
        value: bodyFat!.protein.toStringAsFixed(1),
        unit: 'kg',
        icon: Icons.egg,
        color: Colors.purple,
      ),
      _MetricItem(
        label: 'Minerals',
        value: bodyFat!.minerals.toStringAsFixed(2),
        unit: 'kg',
        icon: Icons.diamond_outlined,
        color: Colors.blueGrey,
      ),
    ];

    // Build 2-column grid manually using Column of Rows
    final rows = <Widget>[];
    for (var i = 0; i < metrics.length; i += 2) {
      if (i > 0) rows.add(const SizedBox(height: 12)); // Vertical spacing

      final item1 = metrics[i];
      final item2 = (i + 1 < metrics.length) ? metrics[i + 1] : null;

      rows.add(Row(
        children: [
          Expanded(child: _buildMetricTile(context, item1)),
          const SizedBox(width: 12), // Horizontal spacing
          if (item2 != null)
            Expanded(child: _buildMetricTile(context, item2))
          else
            const Spacer(), // Empty placeholder for last incomplete row
        ],
      ));
    }

    return Column(children: rows);
  }

  Widget _buildMetricTile(BuildContext context, _MetricItem metric) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final textColor = isDark ? AppTheme.textPrimary : AppTheme.textDark;
    final bgColor = isDark ? AppTheme.backgroundDark : const Color(0xFFF1F5F9);

    return Container(
      width: 100,
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: metric.color.withOpacity(0.3)),
      ),
      child: Column(
        children: [
          Icon(metric.icon, color: metric.color, size: 20),
          const SizedBox(height: 8),
          Text(
            metric.value,
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: textColor,
            ),
          ),
          Text(
            metric.unit,
            style: TextStyle(
              fontSize: 11,
              color: AppTheme.textSecondary.withOpacity(0.7),
            ),
          ),
          const SizedBox(height: 4),
          Text(
            metric.label,
            style: const TextStyle(
              fontSize: 11,
              color: AppTheme.textSecondary,
            ),
          ),
        ],
      ),
    );
  }
}

class _MetricItem {
  final String label;
  final String value;
  final String unit;
  final IconData icon;
  final Color color;

  _MetricItem({
    required this.label,
    required this.value,
    required this.unit,
    required this.icon,
    required this.color,
  });
}
