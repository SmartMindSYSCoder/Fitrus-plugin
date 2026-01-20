import 'package:flutter/material.dart';
import 'package:sm_fitrus/fitrus_model.dart';
import '../theme/app_theme.dart';

/// Connection Status Card - Shows device connection and measurement progress
class ConnectionStatusCard extends StatelessWidget {
  final FitrusModel fitrusModel;
  final VoidCallback? onInit;
  final VoidCallback? onDispose;
  final bool isInitialized;

  const ConnectionStatusCard({
    super.key,
    required this.fitrusModel,
    this.onInit,
    this.onDispose,
    this.isInitialized = false,
  });

  @override
  Widget build(BuildContext context) {
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
          Row(
            children: [
              _buildStatusIndicator(),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'System Status', // Renamed from Device Status
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.w600,
                        color: Theme.of(context).brightness == Brightness.dark
                            ? AppTheme.textPrimary
                            : AppTheme.textDark,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      _getStatusText(),
                      style: TextStyle(
                        fontSize: 14,
                        color: _getStatusColor(),
                      ),
                    ),
                  ],
                ),
              ),
              _buildActionButton(),
            ],
          ),

          // Progress Bar (if measuring)
          if (fitrusModel.hasProgress && fitrusModel.progress > 0) ...[
            const SizedBox(height: 20),
            _buildProgressSection(),
          ],

          // System Status Icons
          if (isInitialized) ...[
            const SizedBox(height: 16),
            const Divider(color: AppTheme.textSecondary, height: 1),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                _buildStatusIcon(context, Icons.bluetooth, 'Bluetooth', true),
                _buildStatusIcon(context, Icons.wifi, 'Internet', true),
                _buildStatusIcon(
                    context, Icons.verified_user, 'Permission', true),
                _buildStatusIcon(
                  context,
                  Icons.watch_outlined,
                  'Device',
                  fitrusModel.isConnected,
                ),
              ],
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildStatusIcon(
      BuildContext context, IconData icon, String label, bool isActive) {
    return Column(
      children: [
        Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: isActive
                ? AppTheme.accentGreen.withOpacity(0.1)
                : AppTheme.textSecondary.withOpacity(0.1),
            shape: BoxShape.circle,
          ),
          child: Icon(
            icon,
            size: 20,
            color: isActive ? AppTheme.accentGreen : AppTheme.textSecondary,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          label,
          style: TextStyle(
            fontSize: 10,
            color: isActive
                ? (Theme.of(context).brightness == Brightness.dark
                    ? AppTheme.textPrimary
                    : AppTheme.textDark)
                : AppTheme.textSecondary,
          ),
        ),
      ],
    );
  }

  Widget _buildStatusIndicator() {
    return Container(
      width: 48,
      height: 48,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        gradient: _getStatusGradient(),
        boxShadow: [
          BoxShadow(
            color: _getStatusColor().withOpacity(0.4),
            blurRadius: 12,
            spreadRadius: 2,
          ),
        ],
      ),
      child: Icon(
        _getStatusIcon(),
        color: Colors.white,
        size: 24,
      ),
    );
  }

  Widget _buildActionButton() {
    if (!isInitialized ||
        fitrusModel.connectionState == FitrusConnectionState.disconnected) {
      return ElevatedButton.icon(
        onPressed: onInit,
        icon: const Icon(Icons.bluetooth_searching, size: 18),
        label: const Text('Connect'),
        style: ElevatedButton.styleFrom(
          backgroundColor: AppTheme.primaryBlue,
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        ),
      );
    }

    if (fitrusModel.connectionState == FitrusConnectionState.connected ||
        fitrusModel.connectionState ==
            FitrusConnectionState.discoveringServices ||
        fitrusModel.connectionState == FitrusConnectionState.dataAvailable ||
        fitrusModel.isConnected) {
      return OutlinedButton.icon(
        onPressed: onDispose,
        icon: const Icon(Icons.bluetooth_disabled, size: 18),
        label: const Text('Disconnect'),
        style: OutlinedButton.styleFrom(
          foregroundColor: AppTheme.errorRed,
          side: const BorderSide(color: AppTheme.errorRed),
        ),
      );
    }

    return const SizedBox.shrink();
  }

  Widget _buildProgressSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            const Text(
              'Measuring...',
              style: TextStyle(
                color: AppTheme.textSecondary,
                fontSize: 13,
              ),
            ),
            Text(
              '${fitrusModel.progress}%',
              style: const TextStyle(
                color: AppTheme.primaryBlue,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        ClipRRect(
          borderRadius: BorderRadius.circular(6),
          child: LinearProgressIndicator(
            value: fitrusModel.progress / 100,
            backgroundColor: AppTheme.backgroundDark,
            valueColor:
                const AlwaysStoppedAnimation<Color>(AppTheme.primaryBlue),
            minHeight: 8,
          ),
        ),
      ],
    );
  }

  String _getStatusText() {
    switch (fitrusModel.connectionState) {
      case FitrusConnectionState.disconnected:
        return isInitialized ? 'Disconnected' : 'Tap Connect to start';
      case FitrusConnectionState.scanning:
        return 'Scanning for devices...';
      case FitrusConnectionState.connecting:
        return 'Connecting...';
      case FitrusConnectionState.connected:
        return 'Connected';
      case FitrusConnectionState.discoveringServices:
        return 'Ready'; // Changed from 'Preparing...'
      case FitrusConnectionState.dataAvailable:
        return 'Data received';
      case FitrusConnectionState.scanFailed:
        return 'Scan failed - try again';
      default:
        if (fitrusModel.rawConnectionState.toLowerCase().contains('error')) {
          return fitrusModel.rawConnectionState;
        }
        return 'Unknown state';
    }
  }

  Color _getStatusColor() {
    switch (fitrusModel.connectionState) {
      case FitrusConnectionState.connected:
      case FitrusConnectionState.discoveringServices:
      case FitrusConnectionState.dataAvailable:
        return AppTheme.accentGreen;
      case FitrusConnectionState.scanning:
      case FitrusConnectionState.connecting:
        return AppTheme.accentOrange;
      case FitrusConnectionState.scanFailed:
        return AppTheme.errorRed;
      default:
        if (fitrusModel.rawConnectionState.toLowerCase().contains('error')) {
          return AppTheme.errorRed;
        }
        return AppTheme.textSecondary;
    }
  }

  LinearGradient _getStatusGradient() {
    final color = _getStatusColor();
    return LinearGradient(
      begin: Alignment.topLeft,
      end: Alignment.bottomRight,
      colors: [color, color.withOpacity(0.7)],
    );
  }

  IconData _getStatusIcon() {
    switch (fitrusModel.connectionState) {
      case FitrusConnectionState.connected:
      case FitrusConnectionState.discoveringServices:
      case FitrusConnectionState.dataAvailable:
        return Icons.bluetooth_connected;
      case FitrusConnectionState.scanning:
        return Icons.bluetooth_searching;
      case FitrusConnectionState.connecting:
        return Icons.bluetooth;
      case FitrusConnectionState.scanFailed:
        return Icons.bluetooth_disabled;
      default:
        if (fitrusModel.rawConnectionState.toLowerCase().contains('error')) {
          return Icons.error_outline;
        }
        return Icons.bluetooth_disabled;
    }
  }
}
